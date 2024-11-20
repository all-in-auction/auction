package com.auction.domain.auction.service;

import com.auction.Point;
import com.auction.PointServiceGrpc;
import com.auction.common.annotation.DistributedLock;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionRankingResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {
    private final ItemRepository itemRepository;
    private final AuctionRepository auctionRepository;

    private final DepositService depositService;
    private final UserService userService;
    private final AuctionItemElasticService elasticService;
    private final AuctionBidGrpcService auctionBidGrpcService;

    private final AuctionPublisher auctionPublisher;
    private final NotificationService notificationService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final PointServiceGrpc.PointServiceBlockingStub pointServiceStub;

    @Value("${notification.related-url.auction}")
    private String relatedAuctionUrl;

    public static final String AUCTION_HISTORY_PREFIX = "auction:bid:";
    public static final String AUCTION_RANKING_PREFIX = "auction:ranking:";

    private Auction getAuctionById(long auctionId) {
        return auctionRepository.findByAuctionId(auctionId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION));
    }

    private Auction getAuctionWithUser(User user, Long auctionId) {
        return auctionRepository.findByIdAndSellerId(auctionId, user.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    @Transactional
    public AuctionCreateResponseDto createAuction(Long userId, AuctionCreateRequestDto requestDto) {
        Item item = Item.of(requestDto.getItem().getName(),
                requestDto.getItem().getDescription(),
                ItemCategory.of(requestDto.getItem().getCategory()));
        Item savedItem = itemRepository.save(item);
        elasticService.saveToElastic(ItemDocument.from(savedItem));

        User user = userService.getUser(userId);
        Auction auction = Auction.of(savedItem, user, requestDto.getMinPrice(), requestDto.isAutoExtension(), requestDto.getExpireAfter());
        Auction savedAuction = auctionRepository.save(auction);

        auctionPublisher.auctionPublisher(
                AuctionEvent.from(savedAuction),
                TimeConverter.toLong(savedAuction.getExpireAt()),
                new Date().getTime()
        );

        return AuctionCreateResponseDto.from(savedAuction);
    }

    @Transactional(readOnly = true)
    public AuctionResponseDto getAuction(Long auctionId) {
        Auction auctionItem = getAuctionById(auctionId);
        return AuctionResponseDto.from(auctionItem);
    }

    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> getAuctionList(Pageable pageable) {
        return auctionRepository.findAllCustom(pageable);
    }

    @Transactional
    public AuctionResponseDto updateAuctionItem(Long userId, Long auctionId, AuctionItemChangeRequestDto requestDto) {
        User user = userService.getUser(userId);
        Auction auction = getAuctionWithUser(user, auctionId);
        Item item = auction.getItem();

        if (requestDto.getName() != null) {
            item.changeName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            item.changeDescription(requestDto.getDescription());
        }
        if (requestDto.getCategory() != null) {
            item.changeCategory(ItemCategory.of(requestDto.getCategory()));
        }

        Item savedItem = itemRepository.save(item);
        auction.changeItem(savedItem);
        elasticService.saveToElastic(ItemDocument.from(savedItem));
        return AuctionResponseDto.from(auction);
    }

    @Transactional
    public String deleteAuctionItem(Long userId, Long auctionId) {
        User user = userService.getUser(userId);
        Auction auction = getAuctionWithUser(user, auctionId);
        elasticService.deleteFromElastic(ItemDocument.from(auction.getItem()));
        auctionRepository.delete(auction);
        return "물품이 삭제되었습니다.";
    }

    @DistributedLock(key = "T(java.lang.String).format('Auction%d', #auctionId)")
    @CircuitBreaker(name = "createBidService", fallbackMethod = "createBidFallback")
    public BidCreateResponseDto createBid(Long userId, Long auctionId, BidCreateRequestDto bidCreateRequestDto) {
        User user = User.fromUserId(userId);
        Auction auction = getAuctionById(auctionId);

        validateBidRequest(user, auction);

        String auctionHistoryKey = AUCTION_HISTORY_PREFIX + auction.getId();

        // 입찰가 변환 : ex) 15999 -> 15000
        int bidPrice = adjustBidPrice(bidCreateRequestDto.getPrice());
        validBidPrice(bidPrice, auction, auctionHistoryKey);

        int pointAmount = auctionBidGrpcService.grpcUserPoint(user.getId());
        validatePointBalance(pointAmount, bidPrice);

        // 마감 5분 전인지 확인하고, 자동연장
        handleAutoExtension(auction);

        auction.changeMaxPrice(bidPrice);
        auctionRepository.save(auction);

        // 포인트 차감, 보증금 예치
        handleDepositAndPoint(user, auctionId, bidPrice);

        // reids zset 에서 이전 최고 입찰 구매자 보증금 환불
        refundPreviousBidder(auctionHistoryKey, user.getId(), auctionId);

        // redis zset 에 입찰 기록 저장
        updateRedis(auctionHistoryKey, user.getId(), bidPrice, auctionId);

        return BidCreateResponseDto.from(user.getId(), auction);
    }

    private void validateBidRequest(User user, Auction auction) {
        if (Objects.equals(auction.getSeller().getId(), user.getId())) {
            throw new ApiException(ErrorStatus._INVALID_BID_REQUEST_USER);
        }

        if (auction.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorStatus._INVALID_BID_CLOSED_AUCTION);
        }
    }

    private int adjustBidPrice(int price) {
        return (price / 1000) * 1000;
    }

    private void validatePointBalance(int pointAmount, int bidPrice) {
        if (pointAmount < bidPrice) {
            throw new ApiException(ErrorStatus._INVALID_NOT_ENOUGH_POINT);
        }
    }

    private void handleAutoExtension(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        boolean isAutoExtensionNow = auction.getExpireAt().minusMinutes(5L).isBefore(now);

        if (auction.isAutoExtension() && isAutoExtensionNow) {
            auction.changeExpireAt(auction.getExpireAt().plusMinutes(10L));
        }
    }

    private void handleDepositAndPoint(User user, Long auctionId, int bidPrice) {
        depositService.getDeposit(user.getId(), auctionId).ifPresentOrElse(
                (deposit) -> {
                    int prevDeposit = Integer.parseInt(deposit);
                    int gap = bidPrice - prevDeposit;

                    auctionBidGrpcService.grpcDecreasePoint(user.getId(), gap);
                    auctionBidGrpcService.createPointHistory(user.getId(), gap, Point.PaymentType.SPEND);
                },
                () -> {
                    auctionBidGrpcService.grpcDecreasePoint(user.getId(), bidPrice);
                    auctionBidGrpcService.createPointHistory(user.getId(), bidPrice, Point.PaymentType.SPEND);
                }
        );
        depositService.placeDeposit(user.getId(), auctionId, bidPrice);
    }

    private void refundPreviousBidder(String auctionHistoryKey, Long currentUserId, Long auctionId) {
        Set<ZSetOperations.TypedTuple<Object>> typedTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(auctionHistoryKey, 0, 0);
        Optional.ofNullable(typedTuples)
                .filter(tuples -> !tuples.isEmpty())
                .ifPresent(tuples -> {
                    for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                        long refundUserId = Long.parseLong(String.valueOf(tuple.getValue()));
                        int price = Objects.requireNonNull(tuple.getScore()).intValue();
                        if (refundUserId != currentUserId) {
                            depositService.deleteDeposit(refundUserId, auctionId);
                            auctionBidGrpcService.increasePoint(refundUserId, price);
                            auctionBidGrpcService.createPointHistory(refundUserId, price, Point.PaymentType.REFUND);
                        }
                    }
                });
    }

    private void updateRedis(String auctionHistoryKey, Long userId, int bidPrice, Long auctionId) {
        redisTemplate.opsForZSet().add(auctionHistoryKey, userId.toString(), bidPrice);
        redisTemplate.opsForZSet().incrementScore(AUCTION_RANKING_PREFIX, String.valueOf(auctionId), 1);
    }

    @CircuitBreaker(name = "grpcUserPoint", fallbackMethod = "grpcUserPointFallback")
    public int grpcUserPoint(long userId) {
        try {
            Point.GetPointsRequest grpcRequest = Point.GetPointsRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            Point.GetPointsResponse pointAmount = pointServiceStub.getPoints(grpcRequest);

            return pointAmount.getTotalPoint();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    @CircuitBreaker(name = "grpcDecreasePoint", fallbackMethod = "grpcDecreasePointFallback")
    public void grpcDecreasePoint(long userId, int amount) {
        try {
            Point.DecreasePointsRequest grpcRequest = Point.DecreasePointsRequest.newBuilder()
                    .setUserId(userId)
                    .setAmount(amount)
                    .build();

            Point.DecreasePointsResponse grpcResponse = pointServiceStub.decreasePoints(grpcRequest);

            if (grpcResponse.getStatus().equalsIgnoreCase("FAILED")) {
                throw new ApiException(ErrorStatus._INVALID_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    @CircuitBreaker(name = "createPointHistory", fallbackMethod = "createPointHistoryFallback")
    public void createPointHistory(long userId, int amount, Point.PaymentType paymentType) {
        try {
            Point.CreatePointHistoryRequest grpcRequest = Point.CreatePointHistoryRequest.newBuilder()
                    .setUserId(userId)
                    .setAmount(amount)
                    .setPaymentType(paymentType)
                    .build();

            Point.CreatePointHistoryResponse grpcResponse = pointServiceStub.createPointHistory(grpcRequest);

            if (grpcResponse.getStatus().equalsIgnoreCase("FAIL")) {
                throw new ApiException(ErrorStatus._INVALID_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    @Transactional
    @CircuitBreaker(name = "closeAuction", fallbackMethod = "closeAuctionFallback")
    public void closeAuction(AuctionEvent auctionEvent) {
        long auctionId = auctionEvent.getAuctionId();
        Auction auction = getAuctionById(auctionId);

        long originExpiredAt = auctionEvent.getExpiredAt();
        long dataSourceExpiredAt = TimeConverter.toLong(auction.getExpireAt());

        String auctionHistoryKey = AUCTION_HISTORY_PREFIX + auctionId;

        // 마감 시간 수정
        if (dataSourceExpiredAt != originExpiredAt) {
            auctionEvent.changeAuctionExpiredAt(dataSourceExpiredAt);
            auctionPublisher.auctionPublisher(auctionEvent, originExpiredAt, dataSourceExpiredAt);
            return;
        }

        // redis key 삭제
        removeAuctionFromRedis(auctionHistoryKey, auctionId);

        Set<Object> topBidder = redisTemplate.opsForZSet().reverseRange(auctionHistoryKey, 0, 0);
        if (isAuctionFailed(topBidder)) {
            // 경매 유찰 알림
            notifyAuctionFailure(auction, auctionId);
        } else {
            // 경매 낙찰
            processAuctionSuccess(auction, topBidder, auctionId);
        }
    }

    private void removeAuctionFromRedis(String auctionHistoryKey, long auctionId) {
        redisTemplate.delete(auctionHistoryKey);
        redisTemplate.opsForZSet().remove(AUCTION_RANKING_PREFIX, String.valueOf(auctionId));
    }

    private boolean isAuctionFailed(Set<Object> bidder) {
        return bidder == null || bidder.isEmpty();
    }

    private void notifyAuctionFailure(Auction auction, long auctionId) {
        notificationService.sendNotification(auction.getSeller(), NotificationType.AUCTION,
                "경매 아이디 " + auctionId + "이(가) 유찰되었습니다.", relatedAuctionUrl + auctionId);
    }

    private void processAuctionSuccess(Auction auction, Set<Object> topBidder, long auctionId) {
        // 판매자 포인트 증가
        auctionBidGrpcService.increasePoint(auction.getSeller().getId(), auction.getMaxPrice());
        auctionBidGrpcService.createPointHistory(auction.getSeller().getId(), auction.getMaxPrice(), Point.PaymentType.RECEIVE);
//            pointService.increasePoint(auction.getSeller().getId(), auction.getMaxPrice());
//            pointHistoryService.createPointHistory(auction.getSeller(), auction.getMaxPrice(), PaymentType.RECEIVE);

        // 구매자 경매 이력 수정
        String buyerId = (String) topBidder.iterator().next();
        User buyer = userService.getUser(Long.parseLong(buyerId));

        auction.changeBuyer(buyer);

        // 보증금 제거
        depositService.deleteDeposit(buyer.getId(), auctionId);
        log.debug("topBidUser : {}", buyer.getId());

        // 경매 낙찰 알림
        notificationService.sendNotification(buyer,
                NotificationType.AUCTION,
                "입찰한 " + auction.getItem().getName() + "이(가) 낙찰되었습니다!",
                relatedAuctionUrl + auctionId);
    }

    @Transactional(readOnly = true)
    public List<AuctionRankingResponseDto> getRankingList() {
        Set<ZSetOperations.TypedTuple<Object>> rankings =
                redisTemplate.opsForZSet().reverseRangeWithScores(AUCTION_RANKING_PREFIX, 0, 9);

        List<AuctionRankingResponseDto> rankingList = new ArrayList<>();

        if (rankings != null) {
            int rank = 1;
            for (ZSetOperations.TypedTuple<Object> ranking : rankings) {
                long auctionId = Long.parseLong(ranking.getValue().toString());
                Auction auction = getAuctionById(auctionId);
                Integer bidCount = ranking.getScore().intValue();

                rankingList.add(AuctionRankingResponseDto.of(rank++, auctionId, bidCount,
                        auction.getItem().getName(), auction.getMaxPrice(),
                        auction.getExpireAt().toString()));
            }
        }
        return rankingList;
    }

    private void validBidPrice(int bidPrice, Auction auction, String auctionHistoryKey) {
        Long size = redisTemplate.opsForZSet().zCard(auctionHistoryKey);
        if (bidPrice < auction.getMaxPrice() // 입찰가가 현재 최대 입찰가보다 낮거나
                || (bidPrice == auction.getMaxPrice() && size != null && size != 0)) { // 최대 입찰가와 같으면
            throw new ApiException(ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }
    }

    // Fallback methods

    private BidCreateResponseDto createBidFallback(Long userId, Long auctionId,
                                                   BidCreateRequestDto bidCreateRequestDto,
                                                   Throwable t) {
        log.error("Fallback for createBid: User={}, Auction={}, Error={}",
                userId, auctionId, t.getMessage());

        return new BidCreateResponseDto();
    }

    private void closeAuctionFallback(AuctionEvent auctionEvent, Throwable t) {
        log.error("Fallback for closeAuction: AuctionEvent={}, Error={}",
                auctionEvent.toString(), t.getMessage());

        log.warn("Auction close event will be retried or handled later.");
    }

    private int grpcUserPointFallback(long userId, Throwable t) {
        log.error("Fallback for grpcUserPoint: userId={}, Error={}", userId, t.getMessage());

        // 기본값 반환
        return 0;
    }

    private void grpcDecreasePointFallback(long userId, int amount, Throwable t) {
        log.error("Fallback triggered for grpcDecreasePoint. User ID: {}, Amount: {}, Error: {}",
                userId, amount, t.getMessage());

        // 실패 기록만 남기고 중단 없이 종료
        log.error("Decrease Point operation failed and will not proceed further.");
    }
}
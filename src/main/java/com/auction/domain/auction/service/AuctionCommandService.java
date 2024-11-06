package com.auction.domain.auction.service;

import com.auction.common.annotation.DistributedLock;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.AuctionHistoryDto;
import com.auction.domain.auction.dto.request.AuctionCacheDto;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuctionCommandService {
    private final ItemRepository itemRepository;
    private final PointRepository pointRepository;
    private final AuctionRepository auctionRepository;

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final DepositService depositService;
    private final UserService userService;

    private final AuctionPublisher auctionPublisher;
    private final NotificationService notificationService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${notification.related-url.auction}")
    private String relatedAuctionUrl;

    public static final String AUCTION_RANKING_PREFIX = "auction:ranking:";
    private static final String AUCTION_HISTORY_PREFIX = "auction:bid:";
    private static final String AUCTION_CACHE_KEY_PREFIX = "auction:";

    @Transactional
    public AuctionCreateResponseDto createAuction(AuthUser authUser, AuctionCreateRequestDto requestDto) {
        Item item = Item.of(requestDto.getItem().getName(), requestDto.getItem().getDescription(), ItemCategory.of(requestDto.getItem().getCategory()));
        Item savedItem = itemRepository.save(item);
        Auction auction = Auction.of(savedItem, User.fromAuthUser(authUser), requestDto.getMinPrice(), requestDto.isAutoExtension(), requestDto.getExpireAfter());
        Auction savedAuction = auctionRepository.save(auction);

        auctionPublisher.auctionPublisher(
                AuctionEvent.from(savedAuction),
                TimeConverter.toLong(savedAuction.getExpireAt()),
                System.currentTimeMillis()
        );

        // 캐시 초기화
        String cacheKey = AUCTION_CACHE_KEY_PREFIX + savedAuction.getId() + ":expireAt:" + savedAuction.getExpireAt();
        redisTemplate.opsForValue().set(cacheKey, AuctionCacheDto.from(savedAuction));

        return AuctionCreateResponseDto.from(savedAuction);
    }

    @DistributedLock(key = "T(java.lang.String).format('Auction%d', #auctionId)")
    public BidCreateResponseDto createBid(AuthUser authUser, long auctionId, BidCreateRequestDto bidCreateRequestDto) {
        User user = User.fromAuthUser(authUser);
        Auction auction = auctionRepository.findByAuctionId(auctionId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION));

        if (Objects.equals(auction.getSeller().getId(), user.getId())) {
            throw new ApiException(ErrorStatus._INVALID_BID_REQUEST_USER);
        }
        if (auction.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorStatus._INVALID_BID_CLOSED_AUCTION);
        }

        String auctionHistoryKey = AUCTION_HISTORY_PREFIX + auction.getId();
        int bidPrice = (bidCreateRequestDto.getPrice() / 1000) * 1000;
//        validBidPrice(bidPrice, auction, auctionHistoryKey);

        int pointAmount = pointRepository.findPointByUserId(user.getId());
        if (pointAmount < bidPrice) {
            throw new ApiException(ErrorStatus._INVALID_NOT_ENOUGH_POINT);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isAutoExtensionNow = auction.getExpireAt().minusMinutes(5L).isBefore(now);
        if (auction.isAutoExtension() && isAutoExtensionNow) {
            auction.changeExpireAt(auction.getExpireAt().plusMinutes(10L));

            // 캐시 내의 경매 '만료시간' 삭제 및 최신화
            String oldCacheKey = AUCTION_CACHE_KEY_PREFIX + auctionId + ":expireAt:" + auction.getExpireAt().minusMinutes(10);
            redisTemplate.delete(oldCacheKey);

            String newCacheKey = AUCTION_CACHE_KEY_PREFIX + auctionId + ":expireAt:" + auction.getExpireAt();
            redisTemplate.opsForValue().set(newCacheKey, auction);
        }

        depositService.getDeposit(user.getId(), auctionId).ifPresentOrElse(
                (deposit) -> {
                    int prevDeposit = Integer.parseInt(deposit.toString());
                    int gap = bidPrice - prevDeposit;
                    pointService.decreasePoint(user.getId(), gap);
                    pointHistoryService.createPointHistory(user, gap, PaymentType.SPEND);
                },
                () -> {
                    pointService.decreasePoint(user.getId(), bidPrice);
                    pointHistoryService.createPointHistory(user, bidPrice, PaymentType.SPEND);
                }
        );
        depositService.placeDeposit(user.getId(), auctionId, bidPrice);

        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(auctionHistoryKey, 0, 0);
        Optional.ofNullable(typedTuples).filter(tuples -> !tuples.isEmpty()).ifPresent(tuples -> {
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                long userId = Long.parseLong(String.valueOf(tuple.getValue()));
                int price = Objects.requireNonNull(tuple.getScore()).intValue();
                if (userId != user.getId()) {
                    AuctionHistoryDto auctionHistoryDto = AuctionHistoryDto.of(userId, price);
                    auctionPublisher.refundPublisher(RefundEvent.from(auctionId, auctionHistoryDto));
                }
            }
        });

        redisTemplate.opsForZSet().add(auctionHistoryKey, user.getId().toString(), bidPrice);
        auction.changeMaxPrice(bidPrice);
        auctionRepository.save(auction);

        return BidCreateResponseDto.of(user.getId(), auction);
    }

    @Transactional
    public void closeAuction(AuctionEvent auctionEvent) {
        long auctionId = auctionEvent.getAuctionId();
        Auction auction = auctionRepository.findByAuctionId(auctionId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION));

        String auctionHistoryKey = AUCTION_HISTORY_PREFIX + auctionId;
        Set<Object> result = redisTemplate.opsForZSet().reverseRange(auctionHistoryKey, 0, 0);

        if (result == null || result.isEmpty()) {
            // 경매 유찰 처리 로직: 입찰자가 없을 경우
            notificationService.sendNotification(
                    auction.getSeller(),
                    NotificationType.AUCTION,
                    "경매 아이디 " + auctionId + "이(가) 유찰되었습니다.",
                    relatedAuctionUrl + auctionId
            );
        } else {
            // 낙찰자 처리 로직: 최고 입찰자가 있을 경우
            String buyerId = (String) result.iterator().next();
            User buyer = userService.getUser(Long.parseLong(buyerId));

            // 낙찰자 설정
            auction.changeBuyer(buyer);

            // 판매자 포인트 증가
            pointService.increasePoint(auction.getSeller().getId(), auction.getMaxPrice());
            pointHistoryService.createPointHistory(
                    auction.getSeller(),
                    auction.getMaxPrice(),
                    PaymentType.RECEIVE
            );

            // 구매자 보증금 제거
            depositService.deleteDeposit(buyer.getId(), auctionId);

            // 낙찰자 알림 전송
            notificationService.sendNotification(
                    buyer,
                    NotificationType.AUCTION,
                    "입찰한 " + auction.getItem().getName() + "이(가) 낙찰되었습니다!",
                    relatedAuctionUrl + auctionId
            );

            // 판매자 알림 전송
            notificationService.sendNotification(
                    auction.getSeller(),
                    NotificationType.AUCTION,
                    "경매 아이디 " + auctionId + "이(가) 성공적으로 낙찰되었습니다.",
                    relatedAuctionUrl + auctionId
            );
        }

        // 입찰 기록 삭제
        redisTemplate.delete(auctionHistoryKey);
        redisTemplate.opsForZSet().remove(AUCTION_RANKING_PREFIX, String.valueOf(auctionId));
    }

    @Transactional
    public AuctionResponseDto updateAuctionItem(AuthUser authUser, Long auctionId, AuctionItemChangeRequestDto requestDto) {
        Auction auction = getAuctionWithUser(authUser, auctionId);
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
        return AuctionResponseDto.from(auction);
    }

    @Transactional
    public String deleteAuctionItem(AuthUser authUser, Long auctionId) {
        Auction auction = getAuctionWithUser(authUser, auctionId);
        auctionRepository.delete(auction);
        return "물품이 삭제되었습니다.";
    }

    private Auction getAuctionWithUser(AuthUser authUser, Long auctionId) {
        return auctionRepository.findByIdAndSellerId(auctionId, authUser.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void resetRankings() {
    redisTemplate.delete(AUCTION_RANKING_PREFIX);
    }

    private void validBidPrice(int bidPrice, Auction auction, String auctionHistoryKey) {
        Long size = redisTemplate.opsForZSet().zCard(auctionHistoryKey);
        if (bidPrice < auction.getMaxPrice() || (bidPrice == auction.getMaxPrice() && size != null && size != 0)) {
            throw new ApiException(ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }
    }
}

package com.auction.domain.auction.service;

import com.auction.Point;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import com.auction.feign.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.auction.data.auction.AuctionMockDataUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private AuctionPublisher auctionPublisher;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuctionBidGrpcService auctionBidGrpcService;
    @Mock
    private DepositService depositService;
    @Mock
    private UserService userService;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @InjectMocks
    private AuctionService auctionService;

    @Test
    public void 연장된_경매() {
        // given
        long auctionId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataSourceExpiredAt = now.plusDays(1);
        long originExpiredAt = TimeConverter.toLong(now);

        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        Auction auction = mock(Auction.class);
        ReflectionTestUtils.setField(auction, "id", auctionId);

        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        when(auction.getExpireAt()).thenReturn(dataSourceExpiredAt);
        when(auctionEvent.getExpiredAt()).thenReturn(originExpiredAt);

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(auctionPublisher).auctionPublisher(eq(auctionEvent), eq(originExpiredAt), eq(TimeConverter.toLong(dataSourceExpiredAt)));
    }

    @Test
    public void 경매_유찰() {
        // given
        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        long auctionId = 1L;
        when(auctionEvent.getAuctionId()).thenReturn(auctionId);
        Auction auction = mock(Auction.class);
        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        when(auction.getExpireAt()).thenReturn(expireAt);
        when(auctionEvent.getExpiredAt()).thenReturn(TimeConverter.toLong(expireAt));
        when(auction.getSeller()).thenReturn(User.fromUserId(1L));

        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForZSet().reverseRange(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptySet());

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(notificationService).sendNotification(
                any(User.class), eq(NotificationType.AUCTION),
                eq("경매 아이디 " + auctionId + "이(가) 유찰되었습니다."), anyString()
        );
    }

    @Test
    public void 경매_낙찰() {
        // given
        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        long auctionId = 1L;
        when(auctionEvent.getAuctionId()).thenReturn(auctionId);
        Auction auction = mock(Auction.class);
        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        when(auction.getExpireAt()).thenReturn(expireAt);
        when(auctionEvent.getExpiredAt()).thenReturn(TimeConverter.toLong(expireAt));

        Long buyerId = 2L;
        User buyer = User.fromUserId(buyerId);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForZSet().reverseRange("auction:bid:1", 0L, 0L)).thenReturn(Set.of(buyerId.toString()));

        Long sellerId = 3L;
        User seller = User.fromUserId(sellerId);
        ReflectionTestUtils.setField(seller, "id", sellerId);
        int maxPrice = 10000;
        when(auction.getSeller()).thenReturn(seller);
        when(auction.getMaxPrice()).thenReturn(maxPrice);
        when(userService.getUser(buyerId)).thenReturn(buyer);

        Item item = Item.of("itemName", "description", ItemCategory.AUTOMOTIVE);
        when(auction.getItem()).thenReturn(item);

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(auctionBidGrpcService).increasePoint(sellerId, maxPrice);
        verify(auctionBidGrpcService).createPointHistory(sellerId, maxPrice, Point.PaymentType.RECEIVE);
        verify(auction).changeBuyer(buyer);
        verify(depositService).deleteDeposit(buyerId, auctionId);
        verify(notificationService).sendNotification(eq(buyer), eq(NotificationType.AUCTION), anyString(), anyString());
    }

    @Nested
    @DisplayName("입찰 등록 테스트")
    public class CreateBidTest {
        @Test
        @DisplayName("존재하지 않는 경매로 인해 입찰 등록에 실패한다.")
        public void bid_notFoundAuction_failure() {
            // given
            long userId = 1L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> auctionService.createBid(userId, auctionId, bidCreateRequestDto));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._NOT_FOUND_AUCTION);
        }

        @Test
        @DisplayName("판매자는 입찰할 수 없어 입찰 등록에 실패한다.")
        public void bid_sellerEqualsBidder_failure() {
            // given
            long userId = 1L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> auctionService.createBid(userId, auctionId, bidCreateRequestDto));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._INVALID_BID_REQUEST_USER);
        }

        @Test
        @DisplayName("마감된 경매라 입찰 등록에 실패한다.")
        public void bid_expiredAuction_failure() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = expiredAuction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> auctionService.createBid(userId, auctionId, bidCreateRequestDto));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._INVALID_BID_CLOSED_AUCTION);
        }

        @Test
        @DisplayName("입찰가가 최대 입찰가보다 낮아 입찰 등록에 실패한다.")
        public void bid_lowerMaxPrice_failure() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(0);

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> auctionService.createBid(userId, auctionId, bidCreateRequestDto));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }

        @Test
        @DisplayName("입찰가가 최대 입찰가와 같지만 최초 입찰이 아니기에 입찰 등록에 실패한다.")
        public void bid_sameMaxPrice_failure() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(1000);

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> auctionService.createBid(userId, auctionId, bidCreateRequestDto));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }

        @Test
        @DisplayName("포인트가 부족해 입찰 등록에 실패한다.")
        public void bid_notEnoughPointAmount_failure() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(1);

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> auctionService.createBid(userId, auctionId, bidCreateRequestDto));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._INVALID_NOT_ENOUGH_POINT);
        }

        @Test
        @DisplayName("자동연장 동의 건은 마감 5분 전에 입찰이 들어오면 자동연장 된다.")
        public void bid_agreeAutoExtension_5minBeforeClosing() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = expiredBefore5MinAuction();
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            assertNotEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("자동연장 동의 건이어도 마감 5분이 아니라면 자동연장 되지 않는다.")
        public void bid_agreeAutoExtension_not5MinBeforeClosing() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            assertEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("마감 5분 전이더라도 자동연장 동의하지 않았다면 자동연장 되지 않는다.")
        public void bid_notAgreeAutoExtension_5minBeforeClosing() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = expiredBefore5MinAuction();
            ReflectionTestUtils.setField(auction, "isAutoExtension", false);
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            assertEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("이전에 입찰했었다면 보증금을 제외하고 추가 예치된다.")
        public void bid_alreadyBiddingDeposit() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            String deposit = "10000";

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.of(deposit));

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            int gapPrice = bidCreateRequestDto().getPrice() - Integer.parseInt(deposit);
            verify(auctionBidGrpcService).grpcDecreasePoint(anyLong(), eq(gapPrice));
            verify(auctionBidGrpcService).createPointHistory(eq(userId), eq(gapPrice), any());
        }

        @Test
        @DisplayName("처음 입찰한다면 작성한 입찰가만큼 보증금으로 차감된다.")
        public void bid_firstTimeBiddingDeposit() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            verify(auctionBidGrpcService).grpcDecreasePoint(anyLong(), eq(bidCreateRequestDto.getPrice()));
            verify(auctionBidGrpcService).createPointHistory(eq(userId), eq(bidCreateRequestDto.getPrice()), any());
        }

        @Test
        @DisplayName("최초 입찰이기에 이전 최고 입찰자는 존재하지 않는다.")
        public void bid_firstTimeBiddingNoRefund() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());
            given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).willReturn(Collections.emptySet());

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            verify(depositService, never()).deleteDeposit(anyLong(), eq(auctionId));
        }

        @Test
        @DisplayName("최초 입찰이 아니라면 이전 최고 입찰자에게 보증금을 환불해준다.")
        public void bid_notFirstTimeBiddingRefund() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(ZSetOperations.TypedTuple.of(1L, 1.0));

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());
            given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).willReturn(tuples);

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            verify(depositService).deleteDeposit(anyLong(), eq(auctionId));
            verify(auctionBidGrpcService).increasePoint(anyLong(), anyInt());
            verify(auctionBidGrpcService).createPointHistory(anyLong(), anyInt(), eq(Point.PaymentType.REFUND));
        }

        @Test
        @DisplayName("입찰 등록에 성공한다.")
        public void bid_success() {
            // given
            long userId = 2L;
            long auctionId = 1L;

            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(ZSetOperations.TypedTuple.of(1L, 1.0));

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(userId, auctionId, bidCreateRequestDto);

            // then
            verify(auctionRepository).save(any());
        }
    }

//    @Test
//    @Transactional
//    @Rollback
//    public void 서비스_로직에서_예외_발생시_카프카_롤백() {
//        // given
//        long auctionId = 1L;
//        AuthUser authUser = new AuthUser(1L, "email@email.com", UserRole.USER);
//        BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(2000);
//
//        // when
//        assertThrows(ApiException.class, () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
//
//        // then : Kafka 메시지가 전송되지 않았는지 검증
//        verify(kafkaTemplate, never()).send(anyString(), any(RefundEvent.class));
//    }

//    @Test
//    @Transactional
//    @Rollback
//    public void 서비스_로직_정상_실행시_카프카_메시지_전송() {
//        // given
//        long auctionId = 1L;
//        AuthUser authUser = new AuthUser(2L, "email@email.com", UserRole.USER);
//        BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(2000);
//
//        Auction auction = auction();
//        Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(
//                ZSetOperations.TypedTuple.of(3L, 1000.0)
//        );
//
//        given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
//        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
//        given(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(0L))).willReturn(tuples);
//        given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
//
//        // when
//        auctionService.createBid(authUser, auctionId, bidCreateRequestDto);
//
//        // then: Kafka 메시지가 특정 토픽에 전송되었는지 검증
//        ArgumentCaptor<RefundEvent> refundEventCaptor = ArgumentCaptor.forClass(RefundEvent.class);
//        verify(kafkaTemplate).send(eq("refund-point-topic"), refundEventCaptor.capture());
//
//        // 전송된 RefundEvent 내용 검증
//        RefundEvent sentEvent = refundEventCaptor.getValue();
//        assertThat(sentEvent.getAuctionId()).isEqualTo(auctionId);
//        assertThat(sentEvent.getUserId()).isEqualTo(3L); // 이전 최고 입찰자의 userId
//        assertThat(sentEvent.getDeposit()).isEqualTo(1000); // 환불 금액 검증
//    }
}
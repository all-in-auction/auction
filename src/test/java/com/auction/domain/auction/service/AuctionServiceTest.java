package com.auction.domain.auction.service;

import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.service.UserService;
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
import static com.auction.data.user.UserMockDataUtil.authUser_ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private PointService pointService;
    @Mock
    private PointHistoryService pointHistoryService;
    @Mock
    private DepositService depositService;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;

    @Mock
    private AuctionPublisher auctionPublisher;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private AuctionService auctionService;

    @Nested
    @DisplayName("입찰 등록 테스트")
    public class CreateBidTest {
        @Test
        @DisplayName("존재하지 않는 경매로 인해 입찰 등록에 실패한다.")
        public void bid_notFoundAuction_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("판매자는 입찰할 수 없어 입찰 등록에 실패한다.")
        public void bid_sellerEqualsBidder_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("마감된 경매라 입찰 등록에 실패한다.")
        public void bid_expiredAuction_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = expiredAuction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("입찰가가 최대 입찰가보다 낮아 입찰 등록에 실패한다.")
        public void bid_lowerMaxPrice_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(0);

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("입찰가가 최대 입찰가와 같지만 최초 입찰이 아니기에 입찰 등록에 실패한다.")
        public void bid_sameMaxPrice_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(1000);

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("포인트가 부족해 입찰 등록에 실패한다.")
        public void bid_notEnoughPointAmount_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(1);

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("자동연장 동의 건은 마감 5분 전에 입찰이 들어오면 자동연장 된다.")
        public void bid_agreeAutoExtension_5minBeforeClosing() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auctionExpiredBefore5Min();
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            assertNotEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("자동연장 동의 건이어도 마감 5분이 아니라면 자동연장 되지 않는다.")
        public void bid_agreeAutoExtension_not5MinBeforeClosing() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            assertEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("마감 5분 전이더라도 자동연장 동의하지 않았다면 자동연장 되지 않는다.")
        public void bid_notAgreeAutoExtension_5minBeforeClosing() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auctionExpiredBefore5Min();
            ReflectionTestUtils.setField(auction, "isAutoExtension", false);
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            assertEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("이전에 입찰했었다면 보증금을 제외하고 추가 예치된다.")
        public void bid_alreadyBiddingDeposit() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            String deposit = "10000";

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.of(deposit));

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            int gapPrice = bidCreateRequestDto().getPrice() - Integer.parseInt(deposit);
            verify(pointService).decreasePoint(anyLong(), eq(gapPrice));
            verify(pointHistoryService).createPointHistory(any(), eq(gapPrice), any());
        }

        @Test
        @DisplayName("처음 입찰한다면 작성한 입찰가만큼 보증금으로 차감된다.")
        public void bid_firstTimeBiddingDeposit() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verify(pointService).decreasePoint(anyLong(), eq(bidCreateRequestDto.getPrice()));
            verify(pointHistoryService).createPointHistory(any(), eq(bidCreateRequestDto.getPrice()), any());
        }

        @Test
        @DisplayName("최초 입찰이기에 이전 최고 입찰자는 존재하지 않는다.")
        public void bid_firstTimeBiddingNoRefund() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());
            given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).willReturn(Collections.emptySet());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verifyNoInteractions(auctionPublisher);
        }

        @Test
        @DisplayName("최초 입찰이 아니라면 이전 최고 입찰자에게 보증금을 환불해준다.")
        public void bid_notFirstTimeBiddingRefund() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(ZSetOperations.TypedTuple.of(1L, 1.0));

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());
            given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).willReturn(tuples);

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verify(auctionPublisher).refundPublisher(any());
        }

        @Test
        @DisplayName("입찰 등록에 성공한다.")
        public void bid_success() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(ZSetOperations.TypedTuple.of(1L, 1.0));

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verify(auctionRepository).save(any());
        }
    }
}
package com.auction.domain.auction.service;

import com.auction.Point;
import com.auction.PointServiceGrpc;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.auction.data.auction.AuctionMockDataUtil.auction;
import static com.auction.data.auction.AuctionMockDataUtil.bidCreateRequestDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CircuitBreakerTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @MockBean
    private AuctionRepository auctionRepository;
    @MockBean
    private AuctionBidGrpcService auctionBidGrpcService;
    @MockBean
    private PointServiceGrpc.PointServiceBlockingStub pointServiceStub;
    @Mock
    private UserService userService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @Autowired
    private AuctionService auctionService;
    private CircuitBreaker circuitBreaker;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class CreateBidServiceTest {

        @BeforeEach
        public void setUp() {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker("createBidService");
        }

        @AfterEach
        public void tearDown() {
            circuitBreaker.reset();
        }

        @Test
        public void normalCase() {
            // given
            Long userId = 2L;
            Long auctionId = 1L;
            BidCreateRequestDto requestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            BidCreateResponseDto response = auctionService.createBid(userId, auctionId, requestDto);

            // then
            assertNotNull(response);
            assertEquals(userId, response.getUserId());
            assertEquals(auctionId, response.getAuctionId());
            verify(auctionRepository).save(any());
        }

        @Test
        public void circuitBreakerTriggered() {
            // given
            Long userId = 2L;
            Long auctionId = 1L;
            BidCreateRequestDto requestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            when(auctionBidGrpcService.grpcUserPoint(anyLong())).thenThrow(new RuntimeException(
                    "Error!! Something's wrong with the point server."
            ));

            // when
            for (int i = 0; i < 10; i++) {
                auctionService.createBid(userId, auctionId, requestDto);
            }

            // then
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }

        @Test
        public void fallBackCalled() {
            // given
            Long userId = 2L;
            Long auctionId = 1L;
            BidCreateRequestDto requestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            when(auctionBidGrpcService.grpcUserPoint(anyLong())).thenThrow(new RuntimeException(
                    "Error!! Something's wrong with the point server."
            ));

            // when
            BidCreateResponseDto response = auctionService.createBid(userId, auctionId, requestDto);

            // then
            assertNotNull(response);
        }

        @Test
        @Disabled
        public void circuitBreakerStateTransitions() throws InterruptedException {
            Long userId = 2L;
            Long auctionId = 1L;
            BidCreateRequestDto requestDto = bidCreateRequestDto();
            Auction auction = auction();
            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(auctionBidGrpcService.grpcUserPoint(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // 초기 상태 - CLOSED
            assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

            // 오류 발생
            for (int i = 0; i < 5; i++) {
                try {
                    int finalI = i;
                    circuitBreaker.executeRunnable(() -> {
                        if (finalI % 2 == 0) { // 50% 확률로 실패
                            throw new RuntimeException("오류 발생!");
                        }
                    });
                } catch (Exception ignored) {
                }
            }

            // OPEN 상태로 전환
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

            // 3초 대기
            Thread.sleep(3000);

            // HALF-OPEN 상태로 전환
            assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
            circuitBreaker.executeRunnable(() -> System.out.println("성공"));
            assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
            circuitBreaker.executeRunnable(() -> System.out.println("성공"));
            // 다시 CLOSED
            assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class GrpcUserPointTest {
        @BeforeEach
        public void setUp() {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker("grpcUserPoint");
        }

        @AfterEach
        public void tearDown() {
            circuitBreaker.reset();
        }

        @Test
        public void circuitBreakerTriggered() {
            // given
            long userId = 2L;
            when(pointServiceStub.getPoints(any(Point.GetPointsRequest.class)))
                    .thenThrow(new RuntimeException("Error!! Something's wrong with the point server."));

            // when
            for (int i = 0; i < 10; i++) {
                auctionService.grpcUserPoint(userId);
            }

            // then
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }

        @Test
        public void fallBackCalled() {
            // given
            long userId = 2L;
            when(pointServiceStub.getPoints(any(Point.GetPointsRequest.class)))
                    .thenThrow(new RuntimeException("Error!! Something's wrong with the point server."));

            // when
            int result = auctionService.grpcUserPoint(userId);

            // then
            assertEquals(0, result);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class GrpcDecreasePointTest {
        @BeforeEach
        public void setUp() {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker("grpcDecreasePoint");
        }

        @AfterEach
        public void tearDown() {
            circuitBreaker.reset();
        }

        @Test
        public void circuitBreakerTriggered() {
            // given
            long userId = 2L;
            int amount = 20000;
            when(pointServiceStub.decreasePoints(any(Point.DecreasePointsRequest.class)))
                    .thenThrow(new RuntimeException("Error!! Something's wrong with the point server."));

            // when
            for (int i = 0; i < 10; i++) {
                auctionService.grpcDecreasePoint(userId, amount);
            }

            // then
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Disabled   // 이미 grpc 내에서 오류 처리가 되어 있어 circuit breaker 붙여도 소용 없는 것 아닌지...
    public class createPointHistoryTest {
        @BeforeEach
        public void setUp() {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker("createPointHistory");
        }

        @AfterEach
        public void tearDown() {
            circuitBreaker.reset();
        }

        @Test
        public void circuitBreakerTriggered() {
            // given
            long userId = 2L;
            int amount = 20000;
            Point.PaymentType paymentType = Point.PaymentType.CHARGE;

            when(pointServiceStub.createPointHistory(any(Point.CreatePointHistoryRequest.class)))
                    .thenThrow(new RuntimeException("Error!! Something's wrong with the point server."));

            // when
            for (int i = 0; i < 10; i++) {
                auctionService.createPointHistory(userId, amount, paymentType);
            }

            // then
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class closeAuctionTest {
        @BeforeEach
        public void setUp() {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker("closeAuction");
        }

        @AfterEach
        public void tearDown() {
            circuitBreaker.reset();
        }

        @Test
        public void circuitBreakerTriggered() {
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
            when(auction.getItem()).thenThrow(new RuntimeException("Error!!"));

            // when
            for (int i = 0; i < 10; i++) {
                auctionService.closeAuction(auctionEvent);
            }

            // then
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }
    }
}
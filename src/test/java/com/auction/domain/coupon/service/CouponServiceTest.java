package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.CouponClaimMessage;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.dto.response.CouponClaimResponseDto;
import com.auction.domain.coupon.dto.response.CouponCreateResponseDto;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.auction.domain.coupon.service.CouponService.COUPON_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {
    @Mock
    private CouponUserRepository couponUserRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponUserService couponUserService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private DefaultRedisScript<Object> redisScript;
    @Mock
    private KafkaTemplate<String, CouponClaimMessage> kafkaTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(couponService, "couponTopic", "test-coupon-topic");
    }

    @Test
    @DisplayName("쿠폰 생성")
    void createCoupon() {
        // given
        CouponCreateRequestDto requestDto = new CouponCreateRequestDto();
        ReflectionTestUtils.setField(requestDto, "amount", 100);
        ReflectionTestUtils.setField(requestDto, "name", "test coupon");
        Coupon coupon = Coupon.from(requestDto);
        ReflectionTestUtils.setField(coupon, "id", 1L);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        CouponCreateResponseDto responseDto = couponService.createCoupon(requestDto);

        // then
        verify(couponRepository, times(1)).save(any(Coupon.class));
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(COUPON_PREFIX + coupon.getId(), coupon.getAmount());

        assertEquals(coupon.getId(), responseDto.getCouponId());
        assertEquals(coupon.getName(), responseDto.getName());
        assertEquals(coupon.getAmount(), responseDto.getAmount());
    }

    @Test
    @DisplayName("쿠폰 발급 v3 - 정상 발급")
    void claimCouponSuccess() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        Long couponId = 1L;
        Coupon coupon = mock(Coupon.class);
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "amount", 100);
        ReflectionTestUtils.setField(coupon, "name", "test coupon");

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(redisTemplate.execute(eq(redisScript), anyList())).thenReturn(1L);

        ArgumentCaptor<CouponClaimMessage> messageCaptor = ArgumentCaptor.forClass(CouponClaimMessage.class);

        // when
        CouponClaimResponseDto responseDto = couponService.claimCouponV3(authUser, couponId);

        // then
        verify(couponRepository, times(1)).findById(couponId);
        verify(redisTemplate, times(1)).execute(eq(redisScript), anyList());
        verify(kafkaTemplate, times(1)).send(eq("test-coupon-topic"), messageCaptor.capture());

        // 검증: CouponClaimMessage
        CouponClaimMessage capturedMessage = messageCaptor.getValue();
        assertEquals(authUser.getId(), capturedMessage.getUserId());
        assertEquals(couponId, capturedMessage.getCouponId());

        // 검증: CouponClaimResponseDto
        assertEquals(coupon.getId(), responseDto.getCouponId());
        assertEquals(coupon.getName(), responseDto.getName());
    }

    @Test
    @DisplayName("쿠폰 발급 v3 - 존재하지 않는 쿠폰")
    void claimNotFoundCoupon() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        Long couponId = 1L;
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> couponService.claimCouponV3(authUser, couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._NOT_FOUND_COUPON);
    }

    @Test
    @DisplayName("쿠폰 발급 v3 - 루아 스크립트 실행 중 예외 발생")
    void createIntervalServerError() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        Long couponId = 1L;
        Coupon coupon = mock(Coupon.class);
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "amount", 100);
        ReflectionTestUtils.setField(coupon, "name", "test coupon");

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        doThrow(new RuntimeException("Redis execution failed"))
                .when(redisTemplate).execute(eq(redisScript), anyList());

        // when
        Throwable throwable = catchThrowable(() -> couponService.claimCouponV3(authUser, couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._INTERNAL_SERVER_ERROR_COUPON);
    }

    @Test
    @DisplayName("쿠폰 발급 v3 - 루아 스크립트 반환 값이 null인 경우")
    void createIntervalServerError2() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        Long couponId = 1L;
        Coupon coupon = mock(Coupon.class);
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "amount", 100);
        ReflectionTestUtils.setField(coupon, "name", "test coupon");

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(redisTemplate.execute(eq(redisScript), anyList())).thenReturn(null);

        // when
        Throwable throwable = catchThrowable(() -> couponService.claimCouponV3(authUser, couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._INTERNAL_SERVER_ERROR_COUPON);
    }

    @Test
    @DisplayName("쿠폰 발급 v3 - 중복 발급 시도")
    void claimAlreadyClaimedCoupon() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        Long couponId = 1L;
        Coupon coupon = mock(Coupon.class);
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "amount", 100);
        ReflectionTestUtils.setField(coupon, "name", "test coupon");

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(redisTemplate.execute(eq(redisScript), anyList())).thenReturn(-100L);

        // when
        Throwable throwable = catchThrowable(() -> couponService.claimCouponV3(authUser, couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._ALREADY_CLAIMED_COUPON);
    }

    @Test
    @DisplayName("쿠폰 발급 v3 - 수량이 소진된 쿠폰")
    void claimSoldOutCoupon() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        Long couponId = 1L;
        Coupon coupon = mock(Coupon.class);
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "amount", 100);
        ReflectionTestUtils.setField(coupon, "name", "test coupon");

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(redisTemplate.execute(eq(redisScript), anyList())).thenReturn(-200L);

        // when
        Throwable throwable = catchThrowable(() -> couponService.claimCouponV3(authUser, couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._SOLD_OUT_COUPON);
    }
}
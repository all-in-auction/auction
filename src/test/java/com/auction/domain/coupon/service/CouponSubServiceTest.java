package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponSubServiceTest {
    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponSubService couponSubService;

    @Test
    @DisplayName("쿠폰 찾기 - 성공")
    void getCouponSuccess() {
        // given
        long couponId = 1L;
        Coupon coupon = Coupon.from(new CouponCreateRequestDto());
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "expireAt", LocalDate.now());
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        // when
        Coupon findCoupon = couponSubService.getCoupon(couponId);

        // then
        assertEquals(coupon.getId(), findCoupon.getId());
    }

    @Test
    @DisplayName("쿠폰 찾기 - 존재하지 않는 쿠폰")
    void createNotFoundCoupon() {
        // given
        long couponId = 1L;
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> couponSubService.getCoupon(couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._NOT_FOUND_COUPON);
    }

    @Test
    @DisplayName("쿠폰 찾기 - 유효기간이 지난 쿠폰")
    void createExpiredCoupon() {
        // given
        long couponId = 1L;
        Coupon coupon = Coupon.from(new CouponCreateRequestDto());
        ReflectionTestUtils.setField(coupon, "id", 1L);
        ReflectionTestUtils.setField(coupon, "expireAt", LocalDate.now().minusDays(1));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        // when
        Throwable throwable = catchThrowable(() -> couponSubService.getCoupon(couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._EXPIRED_COUPON);
    }
}
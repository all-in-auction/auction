package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponUserServiceTest {
    @Mock
    private CouponUserRepository couponUserRepository;
    @Mock
    private CouponSubService couponSubService;

    @InjectMocks
    private CouponUserService couponUserService;

    @Test
    @DisplayName("이미 사용한 쿠폰 재사용시 예외 발생")
    void createCouponException() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        int amount = 10000;
        long couponId = 1L;
        Coupon coupon = new Coupon();
        ReflectionTestUtils.setField(coupon, "discountRate", 10);

        CouponUser couponUser = CouponUser.from(coupon, User.fromAuthUser(authUser));
        ReflectionTestUtils.setField(couponUser, "isAvailable", false);

        given(couponSubService.getCoupon(couponId)).willReturn(coupon);
        when(couponUserRepository.findByUserAndCoupon(any(User.class), any(Coupon.class))).thenReturn(Optional.of(couponUser));

        // when
        Throwable throwable = catchThrowable(() -> couponUserService.getDiscountedAmount(authUser, amount, couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._ALREADY_USED_COUPON);
    }

    @Test
    @DisplayName("쿠폰 정상 사용 - 10% 할인")
    void useCoupon() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        int amount = 10000;
        long couponId = 1L;
        Coupon coupon = new Coupon();
        ReflectionTestUtils.setField(coupon, "discountRate", 10);

        CouponUser couponUser = CouponUser.from(coupon, User.fromAuthUser(authUser));

        given(couponSubService.getCoupon(couponId)).willReturn(coupon);
        when(couponUserRepository.findByUserAndCoupon(any(User.class), any(Coupon.class))).thenReturn(Optional.of(couponUser));

        // when
        int discountedAmount = couponUserService.getDiscountedAmount(authUser, amount, couponId);

        // then
        assertThat(discountedAmount).isEqualTo(9000);
    }

    @Test
    @DisplayName("보유하고 있지 않은 쿠폰 ID로 쿠폰 사용시 예외 발생")
    void createNotOwnedCouponException() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        int amount = 10000;
        long couponId = 1L;
        Coupon coupon = new Coupon();
        ReflectionTestUtils.setField(coupon, "discountRate", 10);

        given(couponSubService.getCoupon(couponId)).willReturn(coupon);
        when(couponUserRepository.findByUserAndCoupon(any(User.class), any(Coupon.class))).thenReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> couponUserService.getDiscountedAmount(authUser, amount, couponId));

        // then
        assertThat(throwable)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._NOT_OWNED_COUPON);
    }

    @Test
    @DisplayName("쿠폰 보유 기록 저장")
    void createCouponUser() {
        // given
        User user = new User();
        Coupon coupon = new Coupon();

        // when
        couponUserService.createCouponUser(user, coupon);

        // then
        ArgumentCaptor<CouponUser> captor = ArgumentCaptor.forClass(CouponUser.class);
        verify(couponUserRepository, times(1)).save(captor.capture());

        // 캡처된 CouponUser 객체를 가져와 검증
        CouponUser capturedCouponUser = captor.getValue();
        assertEquals(user, capturedCouponUser.getUser());
        assertEquals(coupon, capturedCouponUser.getCoupon());
    }
}
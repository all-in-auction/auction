package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.CouponUserDto;
import com.auction.domain.coupon.dto.response.CouponGetResponseDto;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.auction.data.coupon.CouponMockDataUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponUserServiceTest {
    @Mock
    private CouponUserRepository couponUserRepository;

    @InjectMocks
    private CouponUserService couponUserService;

    @Nested
    @DisplayName("유효한 쿠폰 조회 테스트 케이스")
    public class getValidCouponTest {
        @Test
        @DisplayName("내 쿠폰이 아니면 쿠폰 조회에 실패한다.")
        public void getValidCoupon_notOwnedCoupon_failure() {
            // given
            long userId = 1L;
            long couponUserId = 1L;

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> couponUserService.getValidCoupon(userId, couponUserId));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._NOT_OWNED_COUPON);
        }

        @Test
        @DisplayName("만료기간이 지난 쿠폰이면 쿠폰 조회에 실패한다.")
        public void getValidCoupon_expiredCoupon_failure() {
            // given
            long userId = 1L;
            long couponUserId = 1L;

            CouponUserDto couponUserDto = expiredCouponUserDto();
            given(couponUserRepository.getCouponUser(anyLong(), anyLong())).willReturn(Optional.of(couponUserDto));

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> couponUserService.getValidCoupon(userId, couponUserId));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._EXPIRED_COUPON);
        }

        @Test
        @DisplayName("이미 사용된 쿠폰이면 쿠폰 조회에 실패한다.")
        public void getValidCoupon_alreadyUsedCoupon_failure() {
            // given
            long userId = 1L;
            long couponUserId = 1L;

            CouponUserDto couponUserDto = usedCouponUserDto();
            given(couponUserRepository.getCouponUser(anyLong(), anyLong())).willReturn(Optional.of(couponUserDto));

            // when
            Throwable throwable = assertThrows(ApiException.class,
                    () -> couponUserService.getValidCoupon(userId, couponUserId));

            // then
            assertThat(throwable)
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorStatus._ALREADY_USED_COUPON);
        }

        @Test
        @DisplayName("쿠폰 조회에 성공한다.")
        public void getValidCoupon_success() {
            // given
            long userId = 1L;
            long couponUserId = 1L;

            CouponUserDto couponUserDto = couponUserDto();
            given(couponUserRepository.getCouponUser(anyLong(), anyLong())).willReturn(Optional.of(couponUserDto));

            // when
            CouponGetResponseDto couponGetResponseDto = couponUserService.getValidCoupon(userId, couponUserId);

            // then
            assertNotNull(couponGetResponseDto);
        }
    }

    @Test
    @DisplayName("쿠폰 보유 기록 저장")
    void createCouponUser() {
        // given
        User user = User.fromUserId(1L);
        Coupon coupon = mock(Coupon.class);

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
package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponUserService {
    private final CouponUserRepository couponUserRepository;

    private final CouponSubService couponSubService;

    public int getDiscountedAmount(AuthUser authUser, int amount, long couponId) {
        Coupon coupon = couponSubService.getCoupon(couponId);

        CouponUser couponUser = getCouponUser(User.fromAuthUser(authUser), coupon);

        if (!couponUser.isAvailable()) {
            throw new ApiException(ErrorStatus._ALREADY_USED_COUPON);
        }

        return amount * (100 - coupon.getDiscountRate()) / 100;
    }

    public CouponUser getCouponUser(User user, Coupon coupon) {
        return couponUserRepository.findByUserAndCoupon(user, coupon)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_OWNED_COUPON));
    }

    @Transactional
    public void createCouponUser(User user, Coupon coupon) {
        couponUserRepository.save(CouponUser.from(coupon, user));
    }
}

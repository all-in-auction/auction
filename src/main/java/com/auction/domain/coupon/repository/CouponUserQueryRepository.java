package com.auction.domain.coupon.repository;

import com.auction.domain.coupon.dto.CouponUserDto;

import java.util.Optional;

public interface CouponUserQueryRepository {
    Optional<CouponUserDto> getCouponUser(long userId, long couponUserId);
    void useCoupon(long couponUserId, long pointHistoryId);
}

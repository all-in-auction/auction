package com.auction.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponClaimMessage implements Serializable {
    private Long userId;
    private Long couponId;

    public static CouponClaimMessage of(long userId, long couponId) {
        return new CouponClaimMessage(userId, couponId);
    }
}
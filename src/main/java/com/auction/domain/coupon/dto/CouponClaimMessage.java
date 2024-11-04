package com.auction.domain.coupon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class CouponClaimMessage implements Serializable {
    private Long userId;
    private Long couponId;

    public CouponClaimMessage(Long userId, Long couponId) {
        this.userId = userId;
        this.couponId = couponId;
    }
}
package com.auction.domain.coupon.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CouponUserDto {
    private long couponUserId;
    private int discountRate;
    private LocalDateTime usedAt;
    private LocalDate expireAt;

    @QueryProjection
    public CouponUserDto(long couponUserId, int discountRate, LocalDateTime usedAt, LocalDate expireAt) {
        this.couponUserId = couponUserId;
        this.discountRate = discountRate;
        this.usedAt = usedAt;
        this.expireAt = expireAt;
    }
}

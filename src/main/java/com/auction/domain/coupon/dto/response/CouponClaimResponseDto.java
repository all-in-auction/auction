package com.auction.domain.coupon.dto.response;

import com.auction.domain.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponClaimResponseDto {
    private long couponId;
    private LocalDate expireAt;
    private String name;
    private int discountRate;

    public static CouponClaimResponseDto from(Coupon coupon) {
        return new CouponClaimResponseDto(coupon.getId(), coupon.getExpireAt(),
                coupon.getName(), coupon.getDiscountRate());
    }
}

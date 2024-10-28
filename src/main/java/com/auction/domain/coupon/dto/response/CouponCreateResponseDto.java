package com.auction.domain.coupon.dto.response;

import com.auction.domain.coupon.entity.Coupon;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateResponseDto {
    private long couponId;
    private LocalDate expireAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer amount;
    private String name;
    private int discountRate;

    public static CouponCreateResponseDto from(Coupon coupon) {
        return new CouponCreateResponseDto(coupon.getId(), coupon.getExpireAt(),
                coupon.getAmount(), coupon.getName(), coupon.getDiscountRate());
    }
}

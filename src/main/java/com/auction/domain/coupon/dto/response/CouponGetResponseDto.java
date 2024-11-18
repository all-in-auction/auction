package com.auction.domain.coupon.dto.response;

import com.auction.domain.coupon.dto.CouponUserDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponGetResponseDto {
    private int discountRate;

    public static CouponGetResponseDto from(CouponUserDto couponUserDto) {
        return new CouponGetResponseDto(
                couponUserDto.getDiscountRate()
        );
    }
}

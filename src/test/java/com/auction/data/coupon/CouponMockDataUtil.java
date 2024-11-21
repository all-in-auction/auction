package com.auction.data.coupon;

import com.auction.domain.coupon.dto.CouponUserDto;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.entity.Coupon;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CouponMockDataUtil {
    public static CouponCreateRequestDto couponCreateRequestDto() {
        return new CouponCreateRequestDto(LocalDate.now(), 1000, "name", 10);
    }

    public static Coupon coupon() {
        return Coupon.from(couponCreateRequestDto());
    }

    public static Coupon expiredCoupon() {
        CouponCreateRequestDto couponCreateRequestDto = couponCreateRequestDto();
        ReflectionTestUtils.setField(couponCreateRequestDto, "expireAt", LocalDate.now().minusDays(1));

        return Coupon.from(couponCreateRequestDto());
    }

    public static CouponUserDto expiredCouponUserDto() {
        return new CouponUserDto(1L, 10, null, LocalDate.now().minusDays(1));
    }

    public static CouponUserDto usedCouponUserDto() {
        return new CouponUserDto(1L, 10, LocalDateTime.now().minusHours(1), LocalDate.now());
    }

    public static CouponUserDto couponUserDto() {
        return new CouponUserDto(1L, 10, null, LocalDate.now().plusDays(1));
    }
}

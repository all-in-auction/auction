package com.auction.domain.coupon.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.coupon.dto.response.CouponClaimResponseDto;
import com.auction.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    /**
     * 쿠폰 발급
     * @param authUser
     * @param couponId
     * @return
     */
    @PostMapping("/v2/coupons/{couponId}/claim")
    public ApiResponse<CouponClaimResponseDto> claimCoupon(@AuthenticationPrincipal AuthUser authUser,
                                                           @PathVariable Long couponId) {
        return ApiResponse.ok(couponService.claimCoupon(authUser, couponId));
    }

    @PostMapping("/v3/coupons/{couponId}/claim")
    public ApiResponse<CouponClaimResponseDto> claimCouponV3(@AuthenticationPrincipal AuthUser authUser,
                                                           @PathVariable Long couponId) {
        return ApiResponse.ok(couponService.claimCouponV3(authUser, couponId));
    }

}

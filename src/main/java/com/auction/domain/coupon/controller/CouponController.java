package com.auction.domain.coupon.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.coupon.dto.request.CouponUseRequestDto;
import com.auction.domain.coupon.dto.response.CouponClaimResponseDto;
import com.auction.domain.coupon.dto.response.CouponGetResponseDto;
import com.auction.domain.coupon.service.CouponService;
import com.auction.domain.coupon.service.CouponUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final CouponUserService couponUserService;

    /**
     * 쿠폰 발급
     *
     * @param authUser
     * @param couponId
     * @return
     */
    @PostMapping("/v2/coupons/{couponId}/claim")
    public ApiResponse<CouponClaimResponseDto> claimCoupon(@AuthenticationPrincipal AuthUser authUser,
                                                           @PathVariable Long couponId) {
        return ApiResponse.ok(couponService.claimCoupon(authUser, couponId));
    }

    /**
     * 쿠폰 발급
     *
     * @param authUser
     * @param couponId
     * @return
     */
    @PostMapping("/v3/coupons/{couponId}/claim")
    public ApiResponse<CouponClaimResponseDto> claimCouponV3(@AuthenticationPrincipal AuthUser authUser,
                                                             @PathVariable Long couponId) {
        return ApiResponse.ok(couponService.claimCouponV3(authUser, couponId));
    }

    /**
     * 유효한 쿠폰 조회
     *
     * @param userId
     * @param couponUserId
     * @return
     */
    @GetMapping("/internal/v4/coupons/{couponUserId}")
    ApiResponse<CouponGetResponseDto> getValidCoupon(
            @RequestHeader(USER_ID) long userId,
            @PathVariable("couponUserId") Long couponUserId
    ) {
        return ApiResponse.ok(couponUserService.getValidCoupon(userId, couponUserId));
    }

    /**
     * 쿠폰 사용 처리
     *
     * @param userId
     * @param couponUseRequestDto
     * @return
     */
    @PatchMapping("/internal/v4/coupons/{couponUserId}")
    ApiResponse<Void> useCoupon(
            @RequestHeader(USER_ID) long userId,
            @PathVariable("couponUserId") Long couponUserId,
            @RequestBody CouponUseRequestDto couponUseRequestDto
    ) {
        couponUserService.useCoupon(userId, couponUserId, couponUseRequestDto);
        return ApiResponse.ok(null);
    }

}

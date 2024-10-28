package com.auction.domain.coupon.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.dto.response.CouponCreateResponseDto;
import com.auction.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponAdminController {
    private final CouponService couponService;

    /**
     * 쿠폰 생성
     * @param requestDto
     * @return CouponCreateResponseDto
     */
    @PostMapping("/v2/admin/coupons")
    public ApiResponse<CouponCreateResponseDto> createCoupon(
            @Valid @RequestBody CouponCreateRequestDto requestDto) {
        return ApiResponse.created(couponService.createCoupon(requestDto));
    }
}

package com.auction.domain.coupon.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.coupon.dto.request.CouponUseRequestDto;
import com.auction.domain.coupon.dto.response.CouponClaimResponseDto;
import com.auction.domain.coupon.dto.response.CouponCreateResponseDto;
import com.auction.domain.coupon.dto.response.CouponGetResponseDto;
import com.auction.domain.coupon.service.CouponService;
import com.auction.domain.coupon.service.CouponUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "CouponController")
public class CouponController {
    private final CouponService couponService;
    private final CouponUserService couponUserService;

    /**
     * 쿠폰 발급
     *
     * @param userId
     * @param couponId
     * @return
     */
    @PostMapping("/v2/coupons/{couponId}/claim")
    @Operation(summary = "쿠폰 발급", description = "쿠폰 발급받는 API(V2)", hidden = true)
    public ApiResponse<CouponClaimResponseDto> claimCoupon(@RequestHeader(USER_ID) long userId,
                                                           @PathVariable Long couponId) {
        return ApiResponse.ok(couponService.claimCoupon(userId, couponId));
    }

    /**
     * 쿠폰 발급
     *
     * @param userId
     * @param couponId
     * @return
     */
    @PostMapping("/v3/coupons/{couponId}/claim")
    @Operation(summary = "쿠폰 발급", description = "쿠폰 발급받는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "couponId", description = "쿠폰 ID", example = "10")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CouponClaimResponseDto.class)
            )
    )
    public ApiResponse<CouponClaimResponseDto> claimCouponV3(@RequestHeader(USER_ID) long userId,
                                                             @PathVariable Long couponId) {
        return ApiResponse.ok(couponService.claimCouponV3(userId, couponId));
    }

    /**
     * 유효한 쿠폰 조회
     *
     * @param userId
     * @param couponUserId
     * @return
     */
    @GetMapping("/internal/v4/coupons/{couponUserId}")
    @Operation(summary = "쿠폰 조회", description = "유효한 쿠폰 조회하는 API", hidden = true)
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
    @Operation(summary = "쿠폰 사용 처리", description = "쿠폰 사용 처리하는 API", hidden = true)
    ApiResponse<Void> useCoupon(
            @RequestHeader(USER_ID) long userId,
            @PathVariable("couponUserId") Long couponUserId,
            @RequestBody CouponUseRequestDto couponUseRequestDto
    ) {
        couponUserService.useCoupon(userId, couponUserId, couponUseRequestDto);
        return ApiResponse.ok(null);
    }

}

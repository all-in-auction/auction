package com.auction.domain.coupon.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.swagger.ItemDocumentResponsePageDto;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.dto.response.CouponCreateResponseDto;
import com.auction.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "CouponAdminController")
public class CouponAdminController {
    private final CouponService couponService;

    /**
     * 쿠폰 생성
     *
     * @param requestDto
     * @return CouponCreateResponseDto
     */
    @PostMapping("/v2/admin/coupons")
    @Operation(summary = "쿠폰 생성", description = "쿠폰 생성하는 Admin API")
    @Parameters({
            @Parameter(name = "keyword", description = "경매 물품 이름(부분 검색 허용)", example = "시계"),
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CouponCreateResponseDto.class)
            )
    )
    public ApiResponse<CouponCreateResponseDto> createCoupon(
            @Valid @RequestBody CouponCreateRequestDto requestDto) {
        return ApiResponse.created(couponService.createCoupon(requestDto));
    }
}

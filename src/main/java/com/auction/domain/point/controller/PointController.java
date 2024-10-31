package com.auction.domain.point.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.coupon.service.CouponService;
import com.auction.domain.coupon.service.CouponSubService;
import com.auction.domain.coupon.service.CouponUserService;
import com.auction.domain.point.dto.request.ConvertRequestDto;
import com.auction.domain.point.dto.response.ChargeResponseDto;
import com.auction.domain.point.dto.response.ConvertResponseDto;
import com.auction.domain.point.service.PaymentService;
import com.auction.domain.point.service.PointService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class PointController {
    @Value("${payment.client.key}")
    private String CLIENT_KEY;
    private final PaymentService paymentService;
    private final PointService pointService;
    private final CouponUserService couponUserService;
    private final CouponService couponService;
    private final CouponSubService couponSubService;

    /**
     * 포인트 충전
     * @param authUser
     * @param amount
     * @param couponId
     * @param model
     * @return front page
     */
    @GetMapping("/v2/points/buy")
    public String getPaymentPage(@AuthenticationPrincipal AuthUser authUser,
                                 @RequestParam int amount,
                                 @RequestParam(required = false) Long couponId,
                                 Model model) {

        paymentService.validateAmount(amount);

        int paymentAmount = amount;
        CouponUser couponUser = null;
        if (couponId != null) {
            paymentAmount = couponUserService.getDiscountedAmount(authUser, amount, couponId);
            Coupon coupon = couponSubService.getCoupon(couponId);
            couponUser = couponUserService.getCouponUser(User.fromAuthUser(authUser), coupon);
        }

        String orderId = UUID.randomUUID().toString().substring(0, 10);

        model.addAttribute("userId", authUser.getId());
        model.addAttribute("clientKey", CLIENT_KEY);
        model.addAttribute("amount", paymentAmount);
        model.addAttribute("orderId", orderId);

        paymentService.createPayment(orderId, User.fromAuthUser(authUser), amount, paymentAmount, couponUser);
        return "payment/checkout";
    }

    /**
     * 결제 승인
     * 프론트에서 호출
     * @param jsonBody
     * @return ChargeResponseDto
     * @throws IOException
     */
    @PostMapping("/v1/points/buy/confirm")
    @ResponseBody
    public ApiResponse<ChargeResponseDto> confirmPayment(@RequestBody String jsonBody) throws IOException {
        ChargeResponseDto chargeResponseDto = pointService.confirmPayment(jsonBody);

        return ApiResponse.ok(chargeResponseDto);
    }

    /**
     * 포인트 현금 전환
     * @param authUser
     * @param convertRequestDto
     * @return ConvertResponseDto
     */
    @PostMapping("/v1/points/to-cash")
    @ResponseBody
    public ApiResponse<ConvertResponseDto> convertPoint(@AuthenticationPrincipal AuthUser authUser,
                                                        @RequestBody ConvertRequestDto convertRequestDto) {
        return ApiResponse.ok(pointService.convertPoint(authUser, convertRequestDto));
    }
}

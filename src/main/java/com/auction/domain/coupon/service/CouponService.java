package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.dto.response.CouponClaimResponseDto;
import com.auction.domain.coupon.dto.response.CouponCreateResponseDto;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    private final CouponSubService couponSubService;
    private final CouponUserService couponUserService;

    @Transactional
    public CouponCreateResponseDto createCoupon(CouponCreateRequestDto requestDto) {
        Coupon saved = couponRepository.save(Coupon.from(requestDto));

        return CouponCreateResponseDto.from(saved);
    }

    @Transactional
    public CouponClaimResponseDto claimCoupon(AuthUser authUser, Long couponId) {
        Coupon coupon = couponSubService.getCoupon(couponId);

        if (coupon.getAmount() != null && coupon.getAmount() == 0) {
            throw new ApiException(ErrorStatus._SOLD_OUT_COUPON);
        }

        User user = User.fromAuthUser(authUser);

        // 사용자 쿠폰 생성
        couponUserService.createCouponUser(user, coupon);

        // 쿠폰 수량 감소 (동시성 제어 x)
        coupon.decrementAmount();

        return CouponClaimResponseDto.from(coupon);
    }
}

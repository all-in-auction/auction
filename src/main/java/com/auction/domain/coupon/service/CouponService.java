package com.auction.domain.coupon.service;

import com.auction.common.annotation.DistributedLock;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.dto.response.CouponClaimResponseDto;
import com.auction.domain.coupon.dto.response.CouponCreateResponseDto;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;

    private final CouponSubService couponSubService;
    private final CouponUserService couponUserService;

    @Transactional
    public CouponCreateResponseDto createCoupon(CouponCreateRequestDto requestDto) {
        Coupon saved = couponRepository.save(Coupon.from(requestDto));

        return CouponCreateResponseDto.from(saved);
    }

    // 분산락 적용
    @DistributedLock(key = "#couponId")
    public CouponClaimResponseDto claimCoupon(AuthUser authUser, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_COUPON));

        if (coupon.getAmount() != null && coupon.getAmount() == 0) {
            throw new ApiException(ErrorStatus._SOLD_OUT_COUPON);
        }

        User user = User.fromAuthUser(authUser);

        // 쿠폰 중복 발급 불가
        couponUserRepository.findByUserAndCoupon(user, coupon).ifPresent(t -> {
            throw new ApiException(ErrorStatus._ALREADY_CLAIMED_COUPON);
        });

        // 쿠폰 수량 감소
        coupon.decrementAmount();

        // 쿠폰 발급 후 사용자 쿠폰 생성
        couponUserService.createCouponUser(user, coupon);

        return CouponClaimResponseDto.from(coupon);
    }
}

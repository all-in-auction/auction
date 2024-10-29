package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponSubService {
    private final CouponRepository couponRepository;

    public Coupon getCoupon(long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_COUPON));

//        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
//                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_COUPON));

//        Coupon coupon = couponRepository.findByIdWithOptimisticLock(couponId)
//                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_COUPON));

        if (coupon.getExpireAt().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorStatus._EXPIRED_COUPON);
        }

        return coupon;
    }
}

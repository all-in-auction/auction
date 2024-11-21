package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.CouponUserDto;
import com.auction.domain.coupon.dto.request.CouponUseRequestDto;
import com.auction.domain.coupon.dto.response.CouponGetResponseDto;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponUserService {
    private final CouponUserRepository couponUserRepository;

    @Transactional(readOnly = true)
    public CouponGetResponseDto getValidCoupon(long userId, long couponUserId) {
        CouponUserDto couponUserDto = couponUserRepository.getCouponUser(userId, couponUserId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_OWNED_COUPON));

        if (couponUserDto.getExpireAt().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorStatus._EXPIRED_COUPON);
        }

        if (couponUserDto.getUsedAt() != null) {
            throw new ApiException(ErrorStatus._ALREADY_USED_COUPON);
        }

        return CouponGetResponseDto.from(couponUserDto);
    }

    @Transactional
    public void createCouponUser(User user, Coupon coupon) {
        couponUserRepository.save(CouponUser.from(coupon, user));
    }


    @Transactional
    public void useCoupon(long userId, long couponUserId, CouponUseRequestDto couponUseRequestDto) {
        couponUserRepository.useCoupon(couponUserId, couponUseRequestDto.getPointHistoryId());
    }
}

package com.auction.domain.coupon.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.CouponClaimMessage;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponConsumerService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponUserService couponUserService;

    @KafkaListener(topics = "${kafka.topic.coupon}", groupId = "coupon-group")
    @Transactional
    public void handleCouponClaim(CouponClaimMessage message) {
        Long userId = message.getUserId();
        Long couponId = message.getCouponId();

        // 쿠폰 정보와 유저를 가져와서 처리
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_COUPON));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_USER));

        // 남은 수량을 업데이트하고, 쿠폰 발급 내역을 저장
        coupon.decrementAmount();
        couponUserService.createCouponUser(user, coupon);
    }
}

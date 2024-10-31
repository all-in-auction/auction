package com.auction.domain.coupon.service;

import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.coupon.repository.CouponUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponSchedulerService {
    private final CouponUserRepository couponUserRepository;
    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeExpiredCoupons() {
        List<Long> couponIds = couponRepository.findExpiredCouponIds(LocalDate.now().minusDays(1));

        if (couponIds == null || couponIds.isEmpty()) return;

        couponUserRepository.deleteByCouponIds(couponIds);
    }
}

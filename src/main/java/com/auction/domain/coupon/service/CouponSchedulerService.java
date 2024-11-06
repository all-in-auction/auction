package com.auction.domain.coupon.service;

import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.coupon.repository.CouponUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.auction.domain.coupon.service.CouponService.COUPON_PREFIX;
import static com.auction.domain.coupon.service.CouponService.USER_PREFIX;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponSchedulerService {
    private final CouponUserRepository couponUserRepository;
    private final CouponRepository couponRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeExpiredCoupons() {
        List<Long> couponIds = couponRepository.findExpiredCouponIds(LocalDate.now().minusDays(1));

        if (couponIds == null || couponIds.isEmpty()) return;

        // redis 에서 키 삭제
        for (Long couponId : couponIds) {
            String couponKey = COUPON_PREFIX + couponId;
            redisTemplate.delete(couponKey);

            String pattern = couponKey + ":" + USER_PREFIX + "*";
            Set<String> keysToDelete = new HashSet<>();
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
                connection.scan(options).forEachRemaining(key -> keysToDelete.add(new String(key)));
                return null;
            });

            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
            }
        }

        couponUserRepository.deleteByCouponIds(couponIds);
    }
}

package com.auction.domain.coupon.service;

import com.auction.common.annotation.DistributedLock;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.CouponClaimMessage;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.auction.domain.coupon.dto.response.CouponClaimResponseDto;
import com.auction.domain.coupon.dto.response.CouponCreateResponseDto;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;

    private final CouponUserService couponUserService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Object> redisScript;

    public static String COUPON_PREFIX = "COUPON:";
    public static String USER_PREFIX = "USER:";

    @Value("${kafka.topic.coupon}")
    private String couponTopic;
    private final KafkaTemplate<String, CouponClaimMessage> kafkaTemplate;

    @Transactional
    public CouponCreateResponseDto createCoupon(CouponCreateRequestDto requestDto) {
        Coupon saved = couponRepository.save(Coupon.from(requestDto));
        // redis 에 재고 저장
        redisTemplate.opsForValue().set(COUPON_PREFIX + saved.getId(), saved.getAmount());
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

    @Transactional
    public CouponClaimResponseDto claimCouponV3(AuthUser authUser, Long couponId) {
        String couponKey = COUPON_PREFIX + couponId;
        String userKey = couponKey + ":" + USER_PREFIX + authUser.getId();
        List<String> keys = Arrays.asList(couponKey, userKey);

        // Lua 스크립트 실행
        Integer result;
        try {
            result = (Integer) redisTemplate.execute(redisScript, keys);
        } catch (Exception e) {
            throw new ApiException(ErrorStatus._INTERNAL_SERVER_ERROR_COUPON);
        }

        if (result == null) throw new ApiException(ErrorStatus._INTERNAL_SERVER_ERROR_COUPON);
        if (result == -100) throw new ApiException(ErrorStatus._ALREADY_CLAIMED_COUPON);
        if (result == -200) throw new ApiException(ErrorStatus._SOLD_OUT_COUPON);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_COUPON));

        // kafka 메세지 발행
        CouponClaimMessage message = new CouponClaimMessage(authUser.getId(), couponId);
        kafkaTemplate.send(couponTopic, message);

        return CouponClaimResponseDto.from(coupon);
    }
}

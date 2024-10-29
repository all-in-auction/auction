package com.auction.domain.coupon.service;

import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.repository.CouponRepository;
import com.auction.domain.coupon.repository.CouponUserRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import com.auction.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class CouponServiceTest {
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    CouponUserRepository couponUserRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CouponService couponService;
    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void beforeEach() {
        Coupon coupon = new Coupon();
        ReflectionTestUtils.setField(coupon, "expireAt", LocalDate.now());
        ReflectionTestUtils.setField(coupon, "amount", 100);
        ReflectionTestUtils.setField(coupon, "name", "coupon");
        ReflectionTestUtils.setField(coupon, "discountRate", 10);

        couponRepository.save(coupon);

        for (int i = 0; i < 1000; i++) {
            final long userId = i + 1;
            User user = new User();
            ReflectionTestUtils.setField(user, "email", String.valueOf(userId));
            userRepository.save(user);
        }
    }

    // @Test
    @DisplayName("동시에 1000개의 쿠폰 발급 요청 - 락x")
    void claimCoupons() throws InterruptedException {
        // given
        long beforeTime = System.currentTimeMillis();

        final int threadSize = 1000;
        final var executorService = Executors.newFixedThreadPool(threadSize);
        final var countDownLatch = new CountDownLatch(threadSize);

        final long couponId = 1L;

        // when
        for (int i = 0; i < threadSize; i++) {
            final long userId = i + 1;
            final AuthUser authUser = new AuthUser(userId, "email", UserRole.USER);
            executorService.submit(() -> {
                // 쿠폰 service 의 claim 함수를 호출
                try {
                    couponService.claimCoupon(authUser, 1L);
                } catch (ApiException e) {
                    System.out.println(e.getErrorCode().getReasonHttpStatus().getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        // then
        Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();
        Integer remained = updatedCoupon.getAmount();
        int claimedSize = couponUserRepository.findByCoupon(updatedCoupon).size();

        System.out.println("남은 쿠폰 개수: " + remained);
        System.out.println("발급된 쿠폰 개수: " + claimedSize);
        System.out.println(System.currentTimeMillis() - beforeTime + "ms");

        assertEquals(0, updatedCoupon.getAmount()); // 남은 쿠폰 수가 0인지 확인
        assertEquals(100, couponUserRepository.findByCoupon(updatedCoupon).size()); // 발급된 쿠폰 수가 100인지 확인
    }

    // @Test
    @DisplayName("동시에 1000개의 쿠폰 발급 요청 - 비관락")
    void claimCouponsWithPessimisticLock() throws InterruptedException {
        // given
        long beforeTime = System.currentTimeMillis();

        final int threadSize = 1000;
        final var executorService = Executors.newFixedThreadPool(threadSize);
        final var countDownLatch = new CountDownLatch(threadSize);

        final long couponId = 1L;

        // when
        for (int i = 0; i < threadSize; i++) {
            final long userId = i + 1;
            final AuthUser authUser = new AuthUser(userId, "email", UserRole.USER);
            executorService.submit(() -> {
                // 쿠폰 service 의 claim 함수를 호출
                try {
                    couponService.claimCouponWithPessimisticLock(authUser, 1L);
                } catch (ApiException e) {
                    System.out.println(e.getErrorCode().getReasonHttpStatus().getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        // then
        Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();
        Integer remained = updatedCoupon.getAmount();
        int claimedSize = couponUserRepository.findByCoupon(updatedCoupon).size();

        System.out.println("남은 쿠폰 개수: " + remained);
        System.out.println("발급된 쿠폰 개수: " + claimedSize);
        System.out.println(System.currentTimeMillis() - beforeTime + "ms");

        assertEquals(0, updatedCoupon.getAmount()); // 남은 쿠폰 수가 0인지 확인
        assertEquals(100, couponUserRepository.findByCoupon(updatedCoupon).size()); // 발급된 쿠폰 수가 100인지 확인
    }

    // @Test
    @DisplayName("동시에 1000개의 쿠폰 발급 요청 - 분산락")
    void claimCouponsWithDistributedLock() throws InterruptedException {
        // given
        long beforeTime = System.currentTimeMillis();

        final int threadSize = 1000;
        final var executorService = Executors.newFixedThreadPool(threadSize);
        final var countDownLatch = new CountDownLatch(threadSize);

        final long couponId = 1L;

        // when
        for (int i = 0; i < threadSize; i++) {
            final long userId = i + 1;
            final AuthUser authUser = new AuthUser(userId, "email", UserRole.USER);
            executorService.submit(() -> {
                // 쿠폰 service 의 claim 함수를 호출
                try {
                    couponService.claimCouponWithDistributedLock(authUser, 1L);
                } catch (ApiException e) {
                    System.out.println(e.getErrorCode().getReasonHttpStatus().getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        // then
        Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();
        Integer remained = updatedCoupon.getAmount();
        int claimedSize = couponUserRepository.findByCoupon(updatedCoupon).size();

        System.out.println("남은 쿠폰 개수: " + remained);
        System.out.println("발급된 쿠폰 개수: " + claimedSize);
        System.out.println(System.currentTimeMillis() - beforeTime + "ms");

        assertEquals(0, updatedCoupon.getAmount()); // 남은 쿠폰 수가 0인지 확인
        assertEquals(100, couponUserRepository.findByCoupon(updatedCoupon).size()); // 발급된 쿠폰 수가 100인지 확인
    }

    // coupon entity에 version field 추가 필요
    // @Test
    @DisplayName("동시에 1000개의 쿠폰 발급 요청 - 낙관락")
    void claimCouponsWithOptimisticLock() throws InterruptedException {
        // given
        long beforeTime = System.currentTimeMillis();

        final int threadSize = 100;
        final var executorService = Executors.newFixedThreadPool(threadSize);
        final var countDownLatch = new CountDownLatch(threadSize);

        final long couponId = 1L;

        // when
        for (int i = 0; i < threadSize; i++) {
            final long userId = i + 1;
            final AuthUser authUser = new AuthUser(userId, "email", UserRole.USER);
            executorService.submit(() -> {
                // 쿠폰 service 의 claim 함수를 호출
                try {
                    couponService.claimCouponWithOptimisticLock(authUser, 1L);
                } catch (ApiException e) {
                    System.out.println(e.getErrorCode().getReasonHttpStatus().getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        // then
        Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();
        Integer remained = updatedCoupon.getAmount();
        int claimedSize = couponUserRepository.findByCoupon(updatedCoupon).size();

        System.out.println("남은 쿠폰 개수: " + remained);
        System.out.println("발급된 쿠폰 개수: " + claimedSize);
        System.out.println(System.currentTimeMillis() - beforeTime + "ms");

        assertEquals(0, updatedCoupon.getAmount()); // 남은 쿠폰 수가 0인지 확인
        assertEquals(100, couponUserRepository.findByCoupon(updatedCoupon).size()); // 발급된 쿠폰 수가 100인지 확인
    }

}
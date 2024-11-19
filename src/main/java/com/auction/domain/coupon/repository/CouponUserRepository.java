package com.auction.domain.coupon.repository;

import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponUserRepository extends JpaRepository<CouponUser, Long>, CouponUserQueryRepository {
    Optional<CouponUser> findByUserAndCoupon(User user, Coupon coupon);
}

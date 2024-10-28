package com.auction.domain.coupon.repository;

import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository extends JpaRepository<CouponUser, Long> {
    Optional<CouponUser> findByUserAndCoupon(User user, Coupon coupon);

    @Modifying
    @Query("DELETE FROM CouponUser cu WHERE cu.usedAt IS NULL AND cu.coupon.id IN :couponIds")
    void deleteByCouponIds(@Param("couponIds") List<Long> couponIds);
}

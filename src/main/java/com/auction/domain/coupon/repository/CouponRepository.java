package com.auction.domain.coupon.repository;

import com.auction.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query("SELECT c.id FROM Coupon c WHERE c.expireAt = :expireDate")
    List<Long> findExpiredCouponIds(@Param("expireDate") LocalDate expireDate);
}

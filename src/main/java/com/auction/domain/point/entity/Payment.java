package com.auction.domain.point.entity;

import com.auction.domain.coupon.entity.Coupon;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    private int pointAmount;

    private int paymentAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponUserId")
    private CouponUser couponUser;

    private Payment(String orderId, User user, int pointAmount, int paymentAmount, CouponUser couponUser) {
        this.orderId = orderId;
        this.user = user;
        this.pointAmount = pointAmount;
        this.paymentAmount = paymentAmount;
        this.couponUser = couponUser;
    }

    private Payment(String orderId, User user, int pointAmount, int paymentAmount) {
        this.orderId = orderId;
        this.user = user;
        this.pointAmount = pointAmount;
        this.paymentAmount = paymentAmount;
    }

    public static Payment of(String orderId, User user, int pointAmount,
                             int paymentAmount, CouponUser couponUser) {
        if (couponUser != null) {
            return new Payment(orderId, user, pointAmount, paymentAmount, couponUser);
        } else {
            return new Payment(orderId, user, pointAmount, paymentAmount);
        }
    }
}

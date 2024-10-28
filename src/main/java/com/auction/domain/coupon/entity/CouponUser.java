package com.auction.domain.coupon.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.pointHistory.entity.PointHistory;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class CouponUser extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponId")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private LocalDateTime usedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pointHistoryId")
    private PointHistory pointHistory;

    private boolean isAvailable = true;

    public void useCoupon(PointHistory pointHistory) {
        this.pointHistory = pointHistory;
        this.isAvailable = false;
        this.usedAt = LocalDateTime.now();
    }

    private CouponUser(Coupon coupon, User user) {
        this.coupon = coupon;
        this.user = user;
    }

    public static CouponUser from(Coupon coupon, User user) {
        return new CouponUser(coupon, user);
    }
}

package com.auction.domain.coupon.repository;

import com.auction.domain.coupon.dto.CouponUserDto;
import com.auction.domain.coupon.dto.QCouponUserDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.auction.domain.coupon.entity.QCoupon.coupon;
import static com.auction.domain.coupon.entity.QCouponUser.couponUser;
import static com.auction.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class CouponUserQueryRepositoryImpl implements CouponUserQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<CouponUserDto> getCouponUser(long userId, long couponUserId) {
        return Optional.ofNullable(
                jpaQueryFactory.select(
                                new QCouponUserDto(
                                        couponUser.id,
                                        coupon.discountRate,
                                        couponUser.usedAt,
                                        coupon.expireAt
                                )
                        )
                        .from(couponUser)
                        .join(couponUser.coupon, coupon)
                        .join(couponUser.user, user)
                        .where(couponUser.user.id.eq(userId), couponUser.id.eq(couponUserId))
                        .fetchFirst()
        );
    }

    @Override
    public void useCoupon(long couponUserId, long pointHistoryId) {
        jpaQueryFactory.update(couponUser)
                .set(couponUser.isAvailable, false)
                .set(couponUser.usedAt, LocalDateTime.now())
                .set(couponUser.pointHistoryId, pointHistoryId)
                .where(couponUser.id.eq(couponUserId))
                .execute();
    }

}

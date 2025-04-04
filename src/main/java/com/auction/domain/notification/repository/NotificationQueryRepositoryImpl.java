package com.auction.domain.notification.repository;

import com.auction.domain.notification.dto.response.GetNotificationResponseDto;
import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.enums.NotificationType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.auction.domain.notification.entity.QNotification.notification;

public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public NotificationQueryRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<GetNotificationResponseDto> getNotificationListByUserIdAndType(Long userId, String type) {
        queryFactory.update(notification)
                .set(notification.isRead, true)
                .where(userEq(userId), typeEq(type))
                .execute();

        List<Notification> notifications = queryFactory
                .selectFrom(notification)
                .where(userEq(userId), typeEq(type))
                .orderBy(notification.createdAt.desc())
                .fetch();

        return notifications.stream()
                .map(GetNotificationResponseDto::from)
                .toList();
    }

    private BooleanExpression userEq(Long userId) {
        return userId != null ? notification.receiver.id.eq(userId) : null;
    }

    private BooleanExpression typeEq(String type) {
        return type != null ? notification.type.eq(NotificationType.of(type)) : null;
    }
}

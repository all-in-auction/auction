package com.auction.domain.notification.repository;

import com.auction.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationQueryRepository {
}

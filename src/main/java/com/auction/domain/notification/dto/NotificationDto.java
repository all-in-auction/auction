package com.auction.domain.notification.dto;

import com.auction.domain.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.auction.domain.notification.entity.Notification.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long receiverId;
    private String content;
    private String relatedUrl;
    private boolean isRead;
    private NotificationType type;
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(notification.getId(), notification.getReceiver().getId(), notification.getContent(),
                notification.getRelatedUrl(), notification.isRead(), notification.getType(),notification.getCreatedAt());
    }
}

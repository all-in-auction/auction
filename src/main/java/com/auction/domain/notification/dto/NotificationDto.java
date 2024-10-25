package com.auction.domain.notification.dto;

import com.auction.domain.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.auction.domain.notification.entity.Notification.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long receiverId;
    private String content;
    private String relatedUrl;
    private boolean isRead;
    private NotificationType type;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(notification.getReceiver().getId(), notification.getContent(),
                notification.getRelatedUrl(), notification.isRead(), notification.getType(),notification.getCreatedAt());
    }
}

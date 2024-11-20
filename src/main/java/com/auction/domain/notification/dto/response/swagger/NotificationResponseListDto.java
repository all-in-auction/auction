package com.auction.domain.notification.dto.response.swagger;

import com.auction.domain.notification.dto.response.GetNotificationResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "알림 응답 리스트")
public class NotificationResponseListDto {
    @Schema(description = "알림 정보 리스트")
    private List<GetNotificationResponseDto> notifications;

    public NotificationResponseListDto(List<GetNotificationResponseDto> notifications) {
        this.notifications = notifications;
    }
}
package com.auction.domain.notification.repository;

import com.auction.domain.notification.dto.response.GetNotificationResponseDto;

import java.util.List;

public interface NotificationQueryRepository {

    List<GetNotificationResponseDto> getNotificationListByUserIdAndType(Long userId, String type);
}

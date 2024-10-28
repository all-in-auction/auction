package com.auction.domain.notification.repository;

import com.auction.domain.notification.dto.GetNotificationListDto;

import java.util.List;

public interface NotificationQueryRepository {

    List<GetNotificationListDto> getNotificationListByUserIdAndType(Long userId, String type);
}

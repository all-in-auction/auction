package com.auction.domain.notification.service;

import com.auction.common.entity.AuthUser;
import com.auction.domain.notification.dto.GetNotificationListDto;
import com.auction.domain.notification.dto.NotificationDto;
import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.repository.NotificationRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;
    private final RedisMessageService redisMessageService;

    public SseEmitter subscribe(String userId) {
        SseEmitter sseEmitter = sseEmitterService.createEmitter(userId);
        sseEmitterService.send("EventStream Created.", userId, sseEmitter); // send dummy

        redisMessageService.subscribe(userId); // redis 구독

        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> {
            sseEmitterService.deleteEmitter(userId);
            redisMessageService.removeSubscribe(userId); // 구독한 채널 삭제
        });
        return sseEmitter;
    }

    @Transactional
    public void sendNotification(User receiver, NotificationType notificationType, String content, String relatedUrl) {
        Notification notification = notificationRepository.save(
                Notification.of(receiver, content, relatedUrl, notificationType));

        // redis 이벤트 발행
        redisMessageService.publish(receiver.getId().toString(), NotificationDto.from(notification));
    }

    @Transactional
    public List<GetNotificationListDto> getNotificationList(AuthUser authUser, String type) {
        return notificationRepository.getNotificationListByUserIdAndType(authUser.getId(), type);
    }
}

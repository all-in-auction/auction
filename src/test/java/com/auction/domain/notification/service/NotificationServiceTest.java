package com.auction.domain.notification.service;

import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.repository.NotificationRepository;
import com.auction.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SseEmitterService sseEmitterService;
    @Mock
    private RedisMessageService redisMessageService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    public void subscribeTest() {
        // given
        String userId = "1";
        SseEmitter emitter = new SseEmitter();
        when(sseEmitterService.createEmitter(userId)).thenReturn(emitter);

        // when
        SseEmitter resultEmitter = notificationService.subscribe(userId);

        // then
        assertNotNull(resultEmitter);
        verify(sseEmitterService).createEmitter(userId);
        verify(sseEmitterService).send("EventStream Created.", userId, emitter);
        verify(redisMessageService).subscribe(userId);
    }

    @Test
    public void sendNotificationTest() {
        // given
        User receiver = new User();
        ReflectionTestUtils.setField(receiver, "id", 1L);
        String content = "content";
        String relatedUrl = "relatedUrl";
        NotificationType type = NotificationType.AUCTION;
        Notification notification = Notification.of(receiver, content, relatedUrl, type);
        when(notificationRepository.save(notification)).thenReturn(notification);

        // when
        notificationService.sendNotification(receiver, type, content, relatedUrl);

        // then
        verify(notificationRepository).save(notification);
    }
}
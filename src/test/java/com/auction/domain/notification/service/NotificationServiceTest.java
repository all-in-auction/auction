package com.auction.domain.notification.service;

import com.auction.domain.notification.dto.NotificationDto;
import com.auction.domain.notification.dto.response.GetNotificationResponseDto;
import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.repository.NotificationRepository;
import com.auction.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        User receiver = User.fromUserId(1L);
        String content = "content";
        String relatedUrl = "relatedUrl";
        NotificationType type = NotificationType.AUCTION;
        Notification notification = Notification.of(receiver, content, relatedUrl, type);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // when
        notificationService.sendNotification(receiver, type, content, relatedUrl);

        // then
        verify(notificationRepository).save(any(Notification.class));
        verify(redisMessageService).publish(eq("1"), any(NotificationDto.class));
    }

    @Test
    public void getNotificationTest() {
        // given
        long userId = 1L;
        GetNotificationResponseDto dto = new GetNotificationResponseDto("content", "relatedUrl", true, NotificationType.AUCTION, LocalDateTime.now());
        List<GetNotificationResponseDto> list = List.of(dto);
        when(notificationRepository.getNotificationListByUserIdAndType(userId, "auction")).thenReturn(list);

        // when
        List<GetNotificationResponseDto> results = notificationService.getNotificationList(userId, "auction");

        // then
        assertEquals(1, results.size());
        verify(notificationRepository).getNotificationListByUserIdAndType(userId, "auction");
    }
}
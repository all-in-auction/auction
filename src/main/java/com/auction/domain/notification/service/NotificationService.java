package com.auction.domain.notification.service;

import com.auction.domain.notification.dto.NotificationDto;
import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.repository.EmitterRepository;
import com.auction.domain.notification.repository.NotificationRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

import static com.auction.domain.notification.entity.Notification.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    // SSE 연결 지속 시간 (1시간)
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;

    // SseEmitter 식별자 생성, 반환
    private String makeTimeIncludeId(String id) {
        return id + "_" + System.currentTimeMillis();
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("sse")
                    .data(data)
            );
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }
    }

    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, Long userId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheByUserId(String.valueOf(userId));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    // SSE 스트림 통신 연결 생성 및 유지
    public SseEmitter subscribe(Long userId, String lastEventId) {
        String emitterId = makeTimeIncludeId(userId.toString());
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 더미 이벤트 전송. (클라이언트 - 서버 연결 생성 알림 및 유지용)
        String eventId = makeTimeIncludeId(userId.toString());
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userId + "]");

        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, userId, emitterId, emitter);
        }

        return emitter;
    }

    // 알림 생성 및 전송
    public void send(User receiver, NotificationType notificationType, String content, String relatedUrl) {
        Notification notification = notificationRepository.save(of(receiver, content, relatedUrl, notificationType));

        String receiverEmail = receiver.getEmail();
        String eventId = makeTimeIncludeId(receiverEmail);
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterByUserId(receiverEmail); // (2-4)
        emitters.forEach( // (2-5)
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotificationDto.from(notification));
                }
        );
    }

}

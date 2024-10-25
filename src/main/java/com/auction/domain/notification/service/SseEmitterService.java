package com.auction.domain.notification.service;

import com.auction.domain.notification.dto.NotificationDto;
import com.auction.domain.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseEmitterService {
    // sse 연결 지속 시간 (1시간)
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;

    public SseEmitter createEmitter(String emitterId) {
        return emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
    }

    public void deleteEmitter(String emitterId) {
        emitterRepository.deleteById(emitterId);
    }

    public void sendNotification(String emitterId, NotificationDto notificationDto) {
        emitterRepository.findById(emitterId).ifPresent(emitter -> {
            send(notificationDto, emitterId, emitter);
        });
    }

    public void send(Object data, String emitterId, SseEmitter emitter) {
        try {
            log.info("::: send to client id {} : [{}] :::", emitterId, data);
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name("notification")
                    .data(data));
        } catch (Exception e) {
            log.error("Error has occurred while sending notification.", e);
            emitterRepository.deleteById(emitterId);
        }
    }
}

package com.auction.domain.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

public interface EmitterRepository {
    Optional<SseEmitter> findById(String userId);
    SseEmitter save(String emitterId, SseEmitter sseEmitter);
    void deleteById(String emitterId);
}

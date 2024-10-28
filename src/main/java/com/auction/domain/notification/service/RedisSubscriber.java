package com.auction.domain.notification.service;

import com.auction.domain.notification.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisSubscriber implements MessageListener {
    private static final String CHANNEL_PREFIX = "emitter:";

    private final ObjectMapper objectMapper;
    private final SseEmitterService sseEmitterService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel()).substring(CHANNEL_PREFIX.length());
            NotificationDto notificationDto = objectMapper.readValue(message.getBody(), NotificationDto.class);

            sseEmitterService.sendNotification(channel, notificationDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

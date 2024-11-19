package com.auction.domain.notification.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.notification.dto.response.GetNotificationResponseDto;
import com.auction.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestHeader(USER_ID) long userId) {
        return notificationService.subscribe((String.valueOf(userId)));
    }

    @GetMapping(value = "/subscribe2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamMessages(@RequestHeader(USER_ID) long userId) {
        return notificationService.subscribe2((String.valueOf(userId)));
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> ping() {
        return notificationService.ping();
    }

    @GetMapping
    public ApiResponse<List<GetNotificationResponseDto>> getNotificationList(@RequestHeader(USER_ID) long userId,
                                                                             @RequestParam(required = false) String type) {
        return ApiResponse.ok(notificationService.getNotificationList(userId, type));
    }
}

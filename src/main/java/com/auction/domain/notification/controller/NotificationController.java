package com.auction.domain.notification.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.notification.dto.GetNotificationListDto;
import com.auction.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal AuthUser authUser) {
        return notificationService.subscribe(authUser.getId().toString());
    }


    @GetMapping
    public ApiResponse<List<GetNotificationListDto>> getNotificationList(@AuthenticationPrincipal AuthUser authUser,
                                                                         @RequestParam(required = false) String type) {
        return ApiResponse.ok(notificationService.getNotificationList(authUser, type));
    }
}

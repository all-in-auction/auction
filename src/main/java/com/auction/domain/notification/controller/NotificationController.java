package com.auction.domain.notification.controller;

import com.auction.common.entity.AuthUser;
import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal AuthUser authUser) {
        return notificationService.subscribe(authUser.getId().toString());
    }

    
    @GetMapping("/test")
    public void notificationTest(@AuthenticationPrincipal AuthUser authUser) {
        try {
            Thread.sleep(2000);

            notificationService.sendNotification(User.fromAuthUser(authUser), Notification.NotificationType.AUCTION,
                    "this is test notification!!", "https://test-url.com");

            Thread.sleep(2000);

            notificationService.sendNotification(User.fromAuthUser(authUser), Notification.NotificationType.REVIEW,
                    "this is 2nd test notification!!", "https://test-url2.com");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

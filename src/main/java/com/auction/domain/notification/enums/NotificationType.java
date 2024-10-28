package com.auction.domain.notification.enums;

import com.sun.jdi.request.InvalidRequestStateException;

import java.util.Arrays;

public enum NotificationType {
    AUCTION, REVIEW;

    public static NotificationType of(String type) {
        return Arrays.stream(NotificationType.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestStateException("유효하지 않은 타입 입니다."));
    }
}

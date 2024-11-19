package com.auction.domain.notification.enums;

import com.auction.common.enums.Describable;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum NotificationType implements Describable {
    AUCTION("경매"),
    REVIEW("후기");

    private final String description;

    public static NotificationType of(String type) {
        return Arrays.stream(NotificationType.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestStateException("유효하지 않은 타입 입니다."));
    }

    NotificationType(String description) {this.description = description;}
}

package com.auction.domain.user.enums;

import com.auction.common.enums.Describable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@Getter
public enum UserRole implements Describable {

    USER(Authority.USER, "일반 유저"),
    ADMIN(Authority.ADMIN, "관리자");

    private final String userRole;
    private final String description;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 권한입니다"));
    }

    public static class Authority {
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
    }

    UserRole(String userRole, String description) {
        this.userRole = userRole;
        this.description = description;
    }
}

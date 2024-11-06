package com.auction.data.user;

import com.auction.common.entity.AuthUser;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;

public class UserMockDataUtil {
    public static AuthUser authUser_ROLE_USER() {
        return new AuthUser(1L, "email", UserRole.USER);
    }

    public static AuthUser authUser_ROLE_ADMIN() {
        return new AuthUser(2L, "email", UserRole.ADMIN);
    }

}

package com.auction.domain.user.dto.response;

import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String email;
    private String name;
    private String nickName;
    private int zipCode;
    private String address1;
    private String address2;
    private UserRole authority;

    public static UserResponseDto of(User user) {
        return new UserResponseDto(
                user.getEmail(),
                user.getName(),
                user.getNickName(),
                user.getZipCode(),
                user.getAddress1(),
                user.getAddress2(),
                user.getAuthority()
        );
    }
}

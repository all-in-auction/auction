package com.auction.domain.auth.dto.response;

import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDto {

    private String email;
    private String name;
    private String nickName;
    private Integer zipCode;
    private String address1;
    private String address2;
    private UserRole authority;

    public static SignupResponseDto of(User user) {
        return new SignupResponseDto(
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

package com.auction.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String bearerToken;

    public static LoginResponseDto of(String bearerToken) {
        return new LoginResponseDto(bearerToken);
    }
}

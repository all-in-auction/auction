package com.auction.domain.auth.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auth.dto.request.LoginRequestDto;
import com.auction.domain.auth.dto.request.SignoutRequest;
import com.auction.domain.auth.dto.request.SignupRequestDto;
import com.auction.domain.auth.dto.response.LoginResponseDto;
import com.auction.domain.auth.dto.response.SignupResponseDto;
import com.auction.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/v1/auth/signup")
    public ApiResponse<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        return ApiResponse.ok(authService.createUser(signupRequest));
    }

    @PostMapping("/v1/auth/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ApiResponse.ok(authService.login(loginRequestDto));
    }

    @PutMapping("/v1/auth/signout")
    public ApiResponse<Void> deactivateUser(
            @RequestHeader(USER_ID) long userId,
            @RequestBody SignoutRequest signoutRequest) {

        authService.deactivateUser(userId, signoutRequest);
        return ApiResponse.ok(null);
    }
}

package com.auction.domain.user.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.response.ItemResponseDto;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.dto.response.UserResponseDto;
import com.auction.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @PostMapping("/v2/users")
    public ApiResponse<UserResponseDto> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserUpdateRequestDto userUpdateRequest) {

        return ApiResponse.ok(userService.updateUser(authUser, userUpdateRequest));
    }

    @GetMapping("/v2/users/mypage/sales")
    public ApiResponse<List<ItemResponseDto>> getSales(
        @AuthenticationPrincipal AuthUser authUser) {

        return ApiResponse.ok(userService.getSales(authUser));
    }

    @GetMapping("/v2/users/mypage/purchases")
    public ApiResponse<List<ItemResponseDto>> getPurchases(
            @AuthenticationPrincipal AuthUser authUser) {

        return ApiResponse.ok(userService.getPurchases(authUser));
    }
}

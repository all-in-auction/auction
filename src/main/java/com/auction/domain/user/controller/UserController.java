package com.auction.domain.user.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.ItemResponseDto;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.dto.response.UserResponseDto;
import com.auction.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @PostMapping("/v2/users")
    public ApiResponse<UserResponseDto> updateUser(
            @RequestHeader(USER_ID) long userId,
            @RequestBody UserUpdateRequestDto userUpdateRequest) {

        return ApiResponse.ok(userService.updateUser(userId, userUpdateRequest));
    }

    @GetMapping("/v2/users/mypage/sales")
    public ApiResponse<List<ItemResponseDto>> getSales(
            @RequestHeader(USER_ID) long userId) {

        return ApiResponse.ok(userService.getSales(userId));
    }

    @GetMapping("/v2/users/mypage/purchases")
    public ApiResponse<List<ItemResponseDto>> getPurchases(
            @RequestHeader(USER_ID) long userId) {

        return ApiResponse.ok(userService.getPurchases(userId));
    }
}

package com.auction.domain.user.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.ItemResponseDto;
import com.auction.domain.auction.dto.response.swagger.ItemResponseListDto;
import com.auction.domain.review.dto.request.ReviewCreateRequestDto;
import com.auction.domain.review.dto.response.ReviewResponseDto;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.dto.response.UserResponseDto;
import com.auction.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "UserController")
public class UserController {
    private final UserService userService;

    @PostMapping("/v2/users")
    @Operation(summary = "회원정보 수정", description = "회원정보 수정하는 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 회원정보",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = UserUpdateRequestDto.class)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDto.class)
            )
    )
    public ApiResponse<UserResponseDto> updateUser(
            @Parameter(hidden = true) @RequestHeader(USER_ID) long userId,
            @RequestBody UserUpdateRequestDto userUpdateRequest) {

        return ApiResponse.ok(userService.updateUser(userId, userUpdateRequest));
    }

    @GetMapping("/v2/users/mypage/sales")
    @Operation(summary = "판매 물품 조회", description = "판매 물품 목록 조회하는 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemResponseListDto.class)
            )
    )
    public ApiResponse<List<ItemResponseDto>> getSales(
            @Parameter(hidden = true) @RequestHeader(USER_ID) long userId) {

        return ApiResponse.ok(userService.getSales(userId));
    }

    @GetMapping("/v2/users/mypage/purchases")
    @Operation(summary = "구매 물품 조회", description = "구매 물품 목록 조회하는 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemResponseListDto.class)
            )
    )
    public ApiResponse<List<ItemResponseDto>> getPurchases(
            @Parameter(hidden = true) @RequestHeader(USER_ID) long userId) {

        return ApiResponse.ok(userService.getPurchases(userId));
    }
}

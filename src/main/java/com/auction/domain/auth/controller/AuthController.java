package com.auction.domain.auth.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.swagger.ItemDocumentResponsePageDto;
import com.auction.domain.auth.dto.request.LoginRequestDto;
import com.auction.domain.auth.dto.request.SignoutRequest;
import com.auction.domain.auth.dto.request.SignupRequestDto;
import com.auction.domain.auth.dto.response.LoginResponseDto;
import com.auction.domain.auth.dto.response.SignupResponseDto;
import com.auction.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "AuthController")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/v1/auth/signup")
    @Operation(summary = "회원 가입", description = "회원 가입하는 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원 정보",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = SignupRequestDto.class),
                    examples = @ExampleObject(
                            value = """
                            {
                                "email": "example@example.com",
                                "password": "password123",
                                "name": "John Doe",
                                "nickName": "john",
                                "zipCode": 12345,
                                "address1": "123 Main St",
                                "address2": "Apt 4B",
                                "authority": "user"
                            }
                            """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SignupResponseDto.class)
            )
    )
    public ApiResponse<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        return ApiResponse.ok(authService.createUser(signupRequest));
    }

    @PostMapping("/v1/auth/login")
    @Operation(summary = "로그인", description = "로그인하는 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 정보",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = LoginRequestDto.class),
                    examples = @ExampleObject(
                            value = """
                            {
                                "email": "example@example.com",
                                "password": "password123"
                            }
                            """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponseDto.class)
            )
    )
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ApiResponse.ok(authService.login(loginRequestDto));
    }

    @PutMapping("/v1/auth/signout")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴하는 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "탈퇴 회원 정보",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = SignoutRequest.class),
                    examples = @ExampleObject(value = "{\"password\": \"password123\"}")
            )
    )
    public ApiResponse<Void> deactivateUser(
            @Parameter(hidden = true) @RequestHeader(USER_ID) long userId,
            @RequestBody SignoutRequest signoutRequest) {

        authService.deactivateUser(userId, signoutRequest);
        return ApiResponse.ok(null);
    }
}

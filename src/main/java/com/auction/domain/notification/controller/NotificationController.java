package com.auction.domain.notification.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.notification.dto.response.GetNotificationResponseDto;
import com.auction.domain.notification.dto.response.swagger.NotificationResponseListDto;
import com.auction.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications")
@Tag(name = "NotificationController")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "알림 구독", description = "알림 수신을 위해 구독하는 API")
    public SseEmitter subscribe(@Parameter(hidden = true) @RequestHeader(USER_ID) long userId) {
        return notificationService.subscribe((String.valueOf(userId)));
    }

    @GetMapping
    @Operation(summary = "알림 확인", description = "알림 전체 목록 확인하는 API")
    @Parameters({
            @Parameter(name = "type", description = "알림 타입", example = "AUCTION")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationResponseListDto.class)
            )
    )
    public ApiResponse<List<GetNotificationResponseDto>> getNotificationList(@Parameter(hidden = true) @RequestHeader(USER_ID) long userId,
                                                                             @RequestParam(required = false) String type) {
        return ApiResponse.ok(notificationService.getNotificationList(userId, type));
    }
}

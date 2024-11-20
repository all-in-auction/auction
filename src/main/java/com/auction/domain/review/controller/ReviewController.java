package com.auction.domain.review.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.notification.dto.response.GetNotificationResponseDto;
import com.auction.domain.review.dto.request.ReviewCreateRequestDto;
import com.auction.domain.review.dto.request.ReviewUpdateRequestDto;
import com.auction.domain.review.dto.response.ReviewResponseDto;
import com.auction.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "ReviewController")
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping("/v2/auctions/{auctionId}/reviews")
    @Operation(summary = "리뷰 생성", description = "리뷰 생성하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "등록할 리뷰 내용",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ReviewCreateRequestDto.class)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReviewResponseDto.class)
            )
    )
    public ApiResponse<ReviewResponseDto> createReview(@RequestHeader(USER_ID) long userId,
                                                        @PathVariable Long auctionId,
                                                        @RequestBody ReviewCreateRequestDto requestDto) {
        return ApiResponse.created(reviewService.createReview(userId, auctionId, requestDto));
    }

    // 리뷰 수정
    @PutMapping("/v2/auctions/{auctionId}/reviews")
    @Operation(summary = "리뷰 수정", description = "리뷰 수정하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 리뷰 내용",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ReviewUpdateRequestDto.class)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReviewResponseDto.class)
            )
    )
    public ApiResponse<ReviewResponseDto> updateReview(@RequestHeader(USER_ID) long userId,
                                                       @PathVariable Long auctionId,
                                                       @RequestBody ReviewUpdateRequestDto requestDto) {
        return ApiResponse.ok(reviewService.updateReview(userId, auctionId,requestDto));
    }

    // 리뷰 조회
    @GetMapping("/v2/auctions/{auctionId}/reviews")
    @Operation(summary = "리뷰 조회", description = "리뷰 조회하는 API")
    @Parameters({
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReviewResponseDto.class)
            )
    )
    public ApiResponse<ReviewResponseDto> updateReview(@PathVariable Long auctionId) {
        return ApiResponse.ok(reviewService.getReview(auctionId));
    }

    // 리뷰 삭제
    @DeleteMapping("/v2/auctions/{auctionId}/reviews")
    @Operation(summary = "리뷰 삭제", description = "리뷰 삭제하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(value = "{\"message\": \"리뷰가 삭제되었습니다.\"}")
            )
    )
    public ApiResponse<String> deleteReview(@RequestHeader(USER_ID) long userId,
                                          @PathVariable Long auctionId) {
        reviewService.deleteReview(userId, auctionId);
        return ApiResponse.ok("리뷰가 삭제되었습니다.");
    }

}

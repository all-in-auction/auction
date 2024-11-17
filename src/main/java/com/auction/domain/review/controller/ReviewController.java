package com.auction.domain.review.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.review.dto.request.ReviewCreateRequestDto;
import com.auction.domain.review.dto.request.ReviewUpdateRequestDto;
import com.auction.domain.review.dto.response.ReviewResponseDto;
import com.auction.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<ReviewResponseDto> createReview(@RequestHeader(USER_ID) long userId,
                                                        @PathVariable Long auctionId,
                                                        @RequestBody ReviewCreateRequestDto requestDto) {
        return ApiResponse.created(reviewService.createReview(userId, auctionId, requestDto));
    }

    // 리뷰 수정
    @PutMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<ReviewResponseDto> updateReview(@RequestHeader(USER_ID) long userId,
                                                       @PathVariable Long auctionId,
                                                       @RequestBody ReviewUpdateRequestDto requestDto) {
        return ApiResponse.ok(reviewService.updateReview(userId, auctionId,requestDto));
    }

    // 리뷰 조회
    @GetMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<ReviewResponseDto> updateReview(@PathVariable Long auctionId) {
        return ApiResponse.ok(reviewService.getReview(auctionId));
    }

    // 리뷰 삭제
    @DeleteMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<String> deleteReview(@RequestHeader(USER_ID) long userId,
                                          @PathVariable Long auctionId) {
        reviewService.deleteReview(userId, auctionId);
        return ApiResponse.ok("리뷰가 삭제되었습니다.");
    }

}

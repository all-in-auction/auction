package com.auction.domain.review.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionRankingResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.review.dto.request.ReviewCreateRequestDto;
import com.auction.domain.review.dto.request.ReviewUpdateRequestDto;
import com.auction.domain.review.dto.response.ReviewResponseDto;
import com.auction.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<ReviewResponseDto> createReview(@AuthenticationPrincipal AuthUser authUser,
                                                        @PathVariable Long auctionId,
                                                        @RequestBody ReviewCreateRequestDto requestDto) {
        return ApiResponse.created(reviewService.createReview(authUser, auctionId, requestDto));
    }

    // 리뷰 수정
    @PutMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<ReviewResponseDto> updateReview(@AuthenticationPrincipal AuthUser authUser,
                                                       @PathVariable Long auctionId,
                                                       @RequestBody ReviewUpdateRequestDto requestDto) {
        return ApiResponse.ok(reviewService.updateReview(authUser, auctionId,requestDto));
    }

    // 리뷰 조회
    @GetMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<ReviewResponseDto> updateReview(@PathVariable Long auctionId) {
        return ApiResponse.ok(reviewService.getReview(auctionId));
    }

    // 리뷰 삭제
    @DeleteMapping("/v2/auctions/{auctionId}/reviews")
    public ApiResponse<String> deleteReview(@AuthenticationPrincipal AuthUser authUser,
                                          @PathVariable Long auctionId) {
        reviewService.deleteReview(authUser, auctionId);
        return ApiResponse.ok("리뷰가 삭제되었습니다.");
    }

}

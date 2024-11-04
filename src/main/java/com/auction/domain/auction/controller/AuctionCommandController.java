package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.service.AuctionCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/auctions")
@RequiredArgsConstructor
public class AuctionCommandController {
    private final AuctionCommandService auctionCommandService;

    /**
     * 경매 생성
     * @param authUser
     * @param requestDto
     * @return AuctionCreateResponseDto
     */
    @PostMapping
    public ApiResponse<AuctionCreateResponseDto> createAuction(@AuthenticationPrincipal AuthUser authUser,
                                                               @Valid @RequestBody AuctionCreateRequestDto requestDto) {
        return ApiResponse.created(auctionCommandService.createAuction(authUser, requestDto));
    }

    /**
     * 경매 아이템 수정
     * @param authUser
     * @param auctionId
     * @param requestDto
     * @return AuctionResponseDto
     */
    @PutMapping("/{auctionId}")
    public ApiResponse<AuctionResponseDto> updateAuctionItem(@AuthenticationPrincipal AuthUser authUser,
                                                             @PathVariable Long auctionId,
                                                             @Valid @RequestBody AuctionItemChangeRequestDto requestDto) {
        return ApiResponse.ok(auctionCommandService.updateAuctionItem(authUser, auctionId, requestDto));
    }

    /**
     * 경매 삭제
     * @param authUser
     * @param auctionId
     * @return 삭제 메시지
     */
    @DeleteMapping("/{auctionId}")
    public ApiResponse<String> deleteAuctionItem(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long auctionId) {
        return ApiResponse.ok(auctionCommandService.deleteAuctionItem(authUser, auctionId));
    }

    /**
     * 입찰 등록
     * @param authUser
     * @param auctionId 경매 식별자
     * @param bidCreateRequestDto
     * @return BidCreateResponseDto
     */
    @PostMapping("/{auctionId}/bid")
    public ApiResponse<BidCreateResponseDto> createBid(@AuthenticationPrincipal AuthUser authUser,
                                                       @PathVariable("auctionId") Long auctionId,
                                                       @Valid @RequestBody BidCreateRequestDto bidCreateRequestDto) {
        return ApiResponse.ok(auctionCommandService.createBid(authUser, auctionId, bidCreateRequestDto));
    }
}

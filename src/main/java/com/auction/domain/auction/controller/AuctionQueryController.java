package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.AuctionCacheResponseDto;
import com.auction.domain.auction.dto.response.AuctionRankingResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.service.AuctionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v3/auctions")
@RequiredArgsConstructor
public class AuctionQueryController {
    private final AuctionQueryService auctionQueryService;

    /**
     * 경매 단건 조회
     * @param auctionId
     * @return AuctionResponseDto
     */
    @GetMapping("/{auctionId}")
    public ApiResponse<AuctionCacheResponseDto> getAuction(@PathVariable Long auctionId) {
        return ApiResponse.ok(auctionQueryService.getAuction(auctionId));
    }

    /**
     * 경매 전체 조회
     * @param pageable
     * @return Page<AuctionResponseDto>
     */
    @GetMapping
    public ApiResponse<Page<AuctionResponseDto>> getAuctionList(@PageableDefault(size = 5) Pageable pageable) {
        return ApiResponse.ok(auctionQueryService.getAuctionList(pageable));
    }

    /**
     * 경매 조건 검색
     * @param pageable 페이지 조건
     * @param name 경매 이름 검색
     * @param category 카테고리 검색
     * @return Page<AuctionResponseDto>
     */
    @GetMapping("/search")
    public ApiResponse<Page<AuctionResponseDto>> searchAuctionItems(@PageableDefault(size = 5) Pageable pageable,
                                                                    @RequestParam(required = false) String name,
                                                                    @RequestParam(required = false) String category,
                                                                    @RequestParam(required = false) String sortBy) {
        return ApiResponse.ok(auctionQueryService.searchAuctionItems(pageable, name, category, sortBy));
    }

    /**
     * 경매 랭킹 조회
     * @return List<AuctionRankingResponseDto>
     */
    @GetMapping("/rankings")
    public ApiResponse<List<AuctionRankingResponseDto>> getRankingList() {
        return ApiResponse.ok(auctionQueryService.getRankingList());
    }
}

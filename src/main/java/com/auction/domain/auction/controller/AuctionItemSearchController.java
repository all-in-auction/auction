package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import com.auction.domain.auction.service.AuctionSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auctions")
@RequiredArgsConstructor
public class AuctionItemSearchController {

    private final AuctionSearchService searchService;

    /**
     * 조건 검색
     * @param pageable 페이지 조건으로 검색 : ?page=&size=
     * @param keyword 경매 물품 이름으로 검색 (부분 검색 허용) : ?name=
     * @param category 경매 물품 카테고리로 검색 : ?category=
     * @return Page<AuctionResponseDto>
     */
    @GetMapping("/search")
    public ApiResponse<Page<AuctionResponseDto>> searchAuctionItems2(@PageableDefault(size = 5) Pageable pageable,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) String category,
                                                                    @RequestParam(required = false) String sortBy) {
        return ApiResponse.ok(searchService.searchAuctionItemsByKeyword(pageable, keyword, category, sortBy));
    }

    @GetMapping("/elasticsearch")
    public ApiResponse<Page<ItemDocumentResponseDto>> elasticSearchAuctionItems(@PageableDefault(size = 5) Pageable pageable,
                                                                                @RequestParam(required = false) String keyword,
                                                                                @RequestParam(required = false) String category,
                                                                                @RequestParam(required = false) String sortBy) {
//        return ApiResponse.ok(searchService.elasticSearchAuctionItemsByName(pageable, keyword));
        return ApiResponse.ok(searchService.elasticSearchAuctionItemsByKeyword(pageable, keyword, category, sortBy));
    }

}

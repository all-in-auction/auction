package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import com.auction.domain.auction.dto.response.ItemSearchResponseDto;
import com.auction.domain.auction.dto.response.swagger.AuctionItemImageResponseListDto;
import com.auction.domain.auction.dto.response.swagger.ItemDocumentResponsePageDto;
import com.auction.domain.auction.dto.response.swagger.ItemSearchResponsePageDto;
import com.auction.domain.auction.service.AuctionSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequestMapping("/api/v2/auctions")
@RequiredArgsConstructor
@Tag(name = "ItemSearchController")
public class AuctionItemSearchController {

    private final AuctionSearchService searchService;

    /**
     * 조건 검색
     * @param pageable 페이지 조건으로 검색 : ?page=&size=
     * @param keyword 경매 물품 이름으로 검색 (부분 검색 허용) : ?name=
     * @return Page<AuctionResponseDto>
     */
    @GetMapping("/search")
    @Operation(summary = "경매 조건 검색", description = "경매 조건 검색하는 API")
    @Parameters({
            @Parameter(name = "keyword", description = "경매 물품 이름(부분 검색 허용)", example = "시계"),
            @Parameter(name = "page", description = "페이지 번호", example = "0"),
            @Parameter(name = "size", description = "페이지당 항목 수", example = "5")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemSearchResponsePageDto.class)
            )
    )
    public ApiResponse<Page<ItemSearchResponseDto>> searchAuctionItems(@Parameter(hidden = true) @PageableDefault(size = 5) Pageable pageable,
                                                                       @RequestParam(required = false) String keyword) {
        if(keyword == null || keyword.isEmpty()) {
            return ApiResponse.ok(searchService.searchAllAuctionItems(pageable));
        }
        return ApiResponse.ok(searchService.searchAuctionItemsByKeyword(pageable, keyword));
    }

    @GetMapping("/elasticsearch")
    @Operation(summary = "경매 조건 검색(엘라스틱 서치)", description = "경매 조건 검색하는 API")
    @Parameters({
            @Parameter(name = "keyword", description = "경매 물품 이름(부분 검색 허용)", example = "시계"),
            @Parameter(name = "page", description = "페이지 번호", example = "0"),
            @Parameter(name = "size", description = "페이지당 항목 수", example = "5")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemDocumentResponsePageDto.class)
            )
    )
    public ApiResponse<Page<ItemDocumentResponseDto>> elasticSearchAuctionItems(@Parameter(hidden = true) @PageableDefault(size = 5) Pageable pageable,
                                                                                @RequestParam(required = false) String keyword) throws IOException {
        if(keyword == null || keyword.isEmpty()) {
            return ApiResponse.ok(searchService.elasticSearchAllAuctionItems(pageable));
        }
        return ApiResponse.ok(searchService.elasticSearchAuctionItemsByName(pageable, keyword));
    }

}

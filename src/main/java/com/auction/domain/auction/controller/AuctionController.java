package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionRankingResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.dto.response.swagger.AuctionPageResponseDto;
import com.auction.domain.auction.dto.response.swagger.AuctionRankingResponseListDto;
import com.auction.domain.auction.service.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "AuctionController")
public class AuctionController {
    private final AuctionService auctionService;

    /**
     * 경매 생성
     * @param userId
     * @param requestDto
     * @return AuctionCreateResponseDto
     */
    @PostMapping("/v4/auctions")
    @Operation(summary = "경매 생성", description = "경매 생성하는 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = AuctionCreateRequestDto.class),
                    examples = @ExampleObject(value = "{\n" +
                            "  \"minPrice\": 1000,\n" +
                            "  \"autoExtension\": true,\n" +
                            "  \"expireAfter\": \"00:10\",\n" +
                            "  \"item\": {\n" +
                            "    \"name\": \"핸드폰\",\n" +
                            "    \"description\": \"새로운 스마트폰입니다.\",\n" +
                            "    \"category\": \"ELECTRONICS\"\n" +
                            "  }\n" +
                            "}")
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "요청에 성공하였습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuctionCreateResponseDto.class))
    )
    public ApiResponse<AuctionCreateResponseDto> createAuction(@RequestHeader(USER_ID) long userId,
                                                               @Valid @RequestBody AuctionCreateRequestDto requestDto) {
        return ApiResponse.created(auctionService.createAuction(userId, requestDto));
    }

    /**
     * 경매 단건 조회
     * @param auctionId
     * @return AuctionResponseDto
     */
    @GetMapping("/v4/auctions/{auctionId}")
    @Operation(summary = "경매 단건 조회", description = "경매 ID로 조회하는 API")
    @Parameters({
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuctionResponseDto.class)))
    public ApiResponse<AuctionResponseDto> getAuction(@PathVariable Long auctionId) {
        return ApiResponse.ok(auctionService.getAuction(auctionId));
    }

    /**
     * 경매 전체 조회
     * @param pageable
     * @return AuctionResponseDto
     */
    @GetMapping("/v4/auctions")
    @Operation(summary = "경매 전체 조회", description = "경매 목록 조회하는 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuctionPageResponseDto.class)))
    public ApiResponse<Page<AuctionResponseDto>> getAuctionList(@PageableDefault(size = 5, sort = "modifiedAt", direction = Sort.Direction.DESC)
                                                                Pageable pageable)
    {
        return ApiResponse.ok(auctionService.getAuctionList(pageable));
    }

    /**
     * 경매 물품 수정
     * @param userId
     * @param auctionId
     * @param requestDto
     * @return AuctionResponseDto
     */
    @PutMapping("/v4/auctions/{auctionId}")
    @Operation(summary = "경매 물품 수정", description = "경매 물품 수정하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 경매 물품 정보",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = AuctionItemChangeRequestDto.class),
                    examples = @ExampleObject(value = "{\n" +
                            "  \"name\": \"새로운 경매 상품\",\n" +
                            "  \"description\": \"거의 새것 같습니다.\",\n" +
                            "  \"category\": \"ELECTRONICS\"\n" +
                            "}")
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuctionResponseDto.class)))
    public ApiResponse<AuctionResponseDto> updateAuctionItem(@RequestHeader(USER_ID) long userId,
                                                             @PathVariable Long auctionId,
                                                             @Valid @RequestBody AuctionItemChangeRequestDto requestDto) {
        return ApiResponse.ok(auctionService.updateAuctionItem(userId, auctionId, requestDto));
    }

    /**
     * 경매 삭제
     * @param userId
     * @param auctionId
     * @return 삭제 메시지
     */
    @DeleteMapping("/v4/auctions/{auctionId}")
    @Operation(summary = "경매 삭제", description = "경매 삭제하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class), examples = @ExampleObject(value = "{\"message\": \"물품이 삭제되었습니다.\"}")))
    public ApiResponse<String> deleteAuctionItem(@RequestHeader(USER_ID) long userId,
                                                 @PathVariable Long auctionId) {
        return ApiResponse.ok(auctionService.deleteAuctionItem(userId, auctionId));
    }

    /**
     * 입찰 등록
     * @param userId
     * @param auctionId             경매 식별자
     * @param bidCreateRequestDto
     * @return
     */
    @PostMapping("/v4/auctions/{auctionId}/bid")
    @Operation(summary = "입찰 등록", description = "입찰 등록하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "입찰 등록 요청 정보",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = BidCreateRequestDto.class),
                    examples = @ExampleObject(value = "{\n" +
                            "  \"price\": 10000\n" +
                            "}")
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BidCreateResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class),
                            examples = {
                                    @ExampleObject(name = "InvalidBidder", value = "{\"message\": \"경매 등록자는 경매에 참여할 수 없습니다.\"}"),
                                    @ExampleObject(name = "AuctionClosed", value = "{\"message\": \"이미 종료된 경매입니다.\"}"),
                                    @ExampleObject(name = "InsufficientFunds", value = "{\"message\": \"포인트 충전 후 다시 시도해주세요.\"}"),
                                    @ExampleObject(name = "InvalidAmount", value = "{\"message\": \"입찰가는 최고 입찰가보다 높아야 합니다.\"}")
                            }))
    })
    public ApiResponse<BidCreateResponseDto> createBid(
            @RequestHeader(USER_ID) long userId,
            @PathVariable("auctionId") Long auctionId,
            @Valid @RequestBody BidCreateRequestDto bidCreateRequestDto
    ) {
        return ApiResponse.ok(auctionService.createBid(userId, auctionId, bidCreateRequestDto));
    }

    /**
     * 경매 랭킹 조회
     * @return List<AuctionRankingResponseDto>
     */
    @GetMapping("/v4/auctions/rankings")
    @Operation(summary = "경매 랭킹 조회", description = "경매 랭킹 조회하는 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuctionRankingResponseListDto.class)
            ))
    public ApiResponse<List<AuctionRankingResponseDto>> getRankingList() {
        return ApiResponse.ok(auctionService.getRankingList());
    }
}

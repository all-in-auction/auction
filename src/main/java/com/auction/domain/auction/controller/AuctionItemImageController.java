package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.AuctionItemImageResponseDto;
import com.auction.domain.auction.dto.response.swagger.AuctionItemImageResponseListDto;
import com.auction.domain.auction.service.AuctionItemImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions/{auctionId}/items/images")
@Tag(name = "ItemImageController")
public class AuctionItemImageController {

    private final AuctionItemImageService auctionItemImageService;

    /**
     * 경매 물품 사진 등록
     * @param userId
     * @param auctionId
     * @param files
     * @return List<AuctionItemImageResponseDto>
     * @throws IOException
     */
    @PostMapping
    @Operation(summary = "경매 물품 이미지 등록", description = "경매 물품 이미지 등록하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "이미지 업로드 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuctionItemImageResponseListDto.class)
            )
    )
    public ApiResponse<List<AuctionItemImageResponseDto>> uploadImages(@RequestHeader(USER_ID) long userId,
                                                                       @PathVariable("auctionId") Long auctionId,
                                                                       @RequestParam("files") List<MultipartFile> files) throws IOException {
        return ApiResponse.created(auctionItemImageService.uploadImages(userId, auctionId, files));
    }

    /**
     * 경매 물품 삭제
     * @param userId
     * @param auctionId
     * @param imageId
     * @return 삭제 성공 메시지
     */
    @DeleteMapping("/{imageId}")
    @Operation(summary = "경매 물품 이미지 삭제", description = "경매 물품 이미지 삭제하는 API")
    @Parameters({
            @Parameter(name = USER_ID, description = "유저 ID", example = "100000"),
            @Parameter(name = "auctionId", description = "경매 ID", example = "100000"),
            @Parameter(name = "imageId", description = "이미지 ID", example = "10")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "이미지 삭제 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(value = "{\"message\": \"이미지가 삭제되었습니다.\"}")
            )
    )
    public ApiResponse<String> deleteImage(@RequestHeader(USER_ID) long userId,
                                           @PathVariable("auctionId") Long auctionId,
                                           @PathVariable("imageId") Long imageId) {
        return ApiResponse.ok(auctionItemImageService.deleteImages(userId, auctionId, imageId));
    }
}

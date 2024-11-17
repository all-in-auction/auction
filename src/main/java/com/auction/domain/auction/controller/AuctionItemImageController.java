package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.domain.auction.dto.response.AuctionItemImageResponseDto;
import com.auction.domain.auction.service.AuctionItemImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.auction.common.constants.Const.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions/{auctionId}/items/images")
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
    public ApiResponse<String> deleteImage(@RequestHeader(USER_ID) long userId,
                                           @PathVariable("auctionId") Long auctionId,
                                           @PathVariable("imageId") Long imageId) {
        return ApiResponse.ok(auctionItemImageService.deleteImages(userId, auctionId, imageId));
    }
}

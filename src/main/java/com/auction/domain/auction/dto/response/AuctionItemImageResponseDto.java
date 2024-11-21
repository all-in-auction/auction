package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.AuctionItemImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경매 물품 이미지 응답 DTO")
public class AuctionItemImageResponseDto {
    @Schema(description = "이미지 ID", example = "1")
    private Long id;
    @Schema(description = "이미지 경로")
    private String path;
    @Schema(description = "저장된 파일 이름", example = "image1234.jpg")
    private String fileName;
    @Schema(description = "원본 파일 이름", example = "original.jpg")
    private String originName;
    @Schema(description = "파일 확장자", example = "jpg")
    private String extension;

    public static AuctionItemImageResponseDto from(AuctionItemImage auctionItemImage) {
        return new AuctionItemImageResponseDto(
                auctionItemImage.getId(),
                auctionItemImage.getPath(),
                auctionItemImage.getFileName(),
                auctionItemImage.getOriginName(),
                auctionItemImage.getExtension()
        );
    }
}

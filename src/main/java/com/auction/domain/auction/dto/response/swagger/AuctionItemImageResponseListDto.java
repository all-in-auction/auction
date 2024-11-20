package com.auction.domain.auction.dto.response.swagger;

import com.auction.domain.auction.dto.response.AuctionItemImageResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "경매 물품 이미지 응답 리스트")
public class AuctionItemImageResponseListDto {
    @Schema(description = "이미지 응답 리스트")
    private List<AuctionItemImageResponseDto> images;

    public AuctionItemImageResponseListDto(List<AuctionItemImageResponseDto> images) {
        this.images = images;
    }
}

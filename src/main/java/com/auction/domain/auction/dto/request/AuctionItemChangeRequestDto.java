package com.auction.domain.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionItemChangeRequestDto {
    @Schema(description = "경매 물품 이름", example = "새로운 경매 상품")
    private String name;
    @Schema(description = "경매 물품 설명", example = "거의 새것 같습니다.")
    private String description;
    @Schema(description = "카테고리", example = "전자기기")
    private String category;
}

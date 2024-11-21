package com.auction.domain.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateRequestDto {
    @Schema(description = "아이템 이름", example = "핸드폰")
    private String name;
    @Schema(description = "아이템 설명", example = "새로운 스마트폰입니다.")
    private String description;
    @Schema(description = "아이템 카테고리", example = "전자기기")
    private String category;
}

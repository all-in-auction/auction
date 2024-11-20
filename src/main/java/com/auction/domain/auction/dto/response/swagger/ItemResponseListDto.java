package com.auction.domain.auction.dto.response.swagger;

import com.auction.domain.auction.dto.response.ItemResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "물품 응답 정보 리스트")
public class ItemResponseListDto {
    @Schema(description = "물품 정보 리스트")
    private List<ItemResponseDto> items;

    public ItemResponseListDto(List<ItemResponseDto> items) {
        this.items = items;
    }
}

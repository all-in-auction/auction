package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경매 물품 검색 응답 DTO")
public class ItemSearchResponseDto {
    @Schema(description = "물품 ID", example = "1")
    private Long itemId;
    @Schema(description = "물품 이름", example = "빈티지 시계")
    private String name;
    @Schema(description = "물품 설명", example = "유럽산 빈티지 시계입니다.")
    private String description;

    public static ItemSearchResponseDto from(Item item) {
        return new ItemSearchResponseDto(item.getId(), item.getName(), item.getDescription());
    }
}

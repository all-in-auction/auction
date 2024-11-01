package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDocumentResponseDto {
    private Long id;
    private String name;
    private String description;
    private ItemCategory category;

    public static ItemDocumentResponseDto from(ItemDocument itemDocument) {
        return new ItemDocumentResponseDto(itemDocument.getId(), itemDocument.getName(),
                itemDocument.getDescription(), itemDocument.getCategory());
    }
}
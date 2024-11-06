package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchResponseDto {

    private Long itemId;
    private String name;
    private String description;

    public static ItemSearchResponseDto from(Item item) {
        return new ItemSearchResponseDto(item.getId(), item.getName(), item.getDescription());
    }
}

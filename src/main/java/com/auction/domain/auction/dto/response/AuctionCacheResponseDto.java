package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.dto.request.AuctionCacheDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class AuctionCacheResponseDto {
    private final Long id;
    private final String itemName;
    private final int minPrice;
    private final boolean autoExtension;

    public static AuctionCacheResponseDto of(AuctionCacheDto dto) {
        return new AuctionCacheResponseDto(
                dto.getId(),
                dto.getItemName(),
                dto.getMinPrice(),
                dto.isAutoExtension()
        );
    }
}

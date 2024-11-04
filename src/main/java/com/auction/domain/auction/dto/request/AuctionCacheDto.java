package com.auction.domain.auction.dto.request;

import com.auction.domain.auction.entity.Auction;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuctionCacheDto {
    private Long id;
    private String itemName;
    private int minPrice;
    private boolean autoExtension;

    public static AuctionCacheDto from(Auction auction) {
        AuctionCacheDto dto = new AuctionCacheDto();
        dto.id = auction.getId();
        dto.itemName = auction.getItem().getName();
        dto.minPrice = auction.getMinPrice();
        dto.autoExtension = auction.isAutoExtension();
        return dto;
    }
}

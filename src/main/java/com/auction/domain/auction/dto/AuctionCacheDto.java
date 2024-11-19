package com.auction.domain.auction.dto;

import com.auction.domain.auction.entity.Auction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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

package com.auction.domain.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionRankingResponseDto {
    private Integer rank;
    private Long auctionId;
    private int bidCount;
    private String itemName;
    private int maxPrice;
    private String expireAt;

    public static AuctionRankingResponseDto of(Integer rank, Long auctionId, int bidCount,
                                               String itemName, int maxPrice, String expireAt) {
        return new AuctionRankingResponseDto(rank, auctionId, bidCount, itemName, maxPrice, expireAt);
    }
}

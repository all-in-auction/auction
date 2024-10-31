package com.auction.domain.auction.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuctionHistoryDto {
    private long userId;
    private int price;

    private AuctionHistoryDto(long userId, int price) {
        this.userId = userId;
        this.price = price;
    }

    public static AuctionHistoryDto of(long userId, int price) {
        return new AuctionHistoryDto(userId, price);
    }
}

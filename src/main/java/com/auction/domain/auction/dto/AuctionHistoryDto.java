package com.auction.domain.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionHistoryDto {
    private long userId;
    private int price;

    public static AuctionHistoryDto of(long userId, int price) {
        return new AuctionHistoryDto(userId, price);
    }
}

package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BidCreateResponseDto {
    private long auctionId;
    private long userId;
    private String name;
    private ItemCategory category;
    private String description;
    private LocalDateTime expireAt;
    private int minPrice;
    private int maxPrice;
    private boolean isAutoExtension;

    public static BidCreateResponseDto from(long userId, Auction auction) {
        return new BidCreateResponseDto(
                auction.getId(),
                userId,
                auction.getItem().getName(),
                auction.getItem().getCategory(),
                auction.getItem().getDescription(),
                auction.getExpireAt(),
                auction.getMinPrice(),
                auction.getMaxPrice(),
                auction.isAutoExtension()
        );
    }
}
package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Auction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경매 응답 DTO")
public class AuctionResponseDto {
    @Schema(description = "경매 ID", example = "1")
    private Long auctionId;
    @Schema(description = "판매자 ID", example = "10")
    private Long sellerId;
    @Schema(description = "구매자 ID", example = "20")
    private Long buyerId;
    @Schema(description = "최소 입찰 금액", example = "1000")
    private int minPrice;
    @Schema(description = "최대 입찰 금액", example = "5000")
    private int maxPrice;
    @Schema(description = "판매 완료 여부", example = "false")
    private boolean isSold;
    @Schema(description = "자동 연장 여부", example = "true")
    private boolean isAutoExtension;
    @Schema(description = "경매 생성 시간", example = "2024-11-20T10:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "경매 만료 시간", example = "2024-11-25T10:00:00")
    private LocalDateTime expireAt;
    @Schema(description = "경매 상품 정보")
    private ItemResponseDto item;

    public static AuctionResponseDto from(Auction auction) {
        return new AuctionResponseDto(auction.getId(), auction.getSeller().getId(), auction.getBuyerId(),
                auction.getMinPrice(), auction.getMaxPrice(), auction.isSold(), auction.isAutoExtension(),
                auction.getCreatedAt(), auction.getExpireAt(), ItemResponseDto.from(auction.getItem()));
    }
}

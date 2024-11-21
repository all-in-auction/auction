package com.auction.domain.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경매 랭킹 응답 DTO")
public class AuctionRankingResponseDto {
    @Schema(description = "순위", example = "1")
    private Integer rank;
    @Schema(description = "경매 ID", example = "10001")
    private Long auctionId;
    @Schema(description = "입찰 횟수", example = "15")
    private int bidCount;
    @Schema(description = "상품 이름", example = "빈티지 시계")
    private String itemName;
    @Schema(description = "최고 입찰 금액", example = "150000")
    private int maxPrice;
    @Schema(description = "경매 만료 시각", example = "2024-11-25T10:00:00")
    private String expireAt;

    public static AuctionRankingResponseDto of(Integer rank, Long auctionId, int bidCount,
                                               String itemName, int maxPrice, String expireAt) {
        return new AuctionRankingResponseDto(rank, auctionId, bidCount, itemName, maxPrice, expireAt);
    }
}

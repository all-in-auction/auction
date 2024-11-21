package com.auction.domain.auction.dto.response.swagger;

import com.auction.domain.auction.dto.response.AuctionRankingResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "경매 랭킹 응답 리스트")
public class AuctionRankingResponseListDto {
    @Schema(description = "경매 랭킹 리스트")
    private List<AuctionRankingResponseDto> rankings;

    public AuctionRankingResponseListDto(List<AuctionRankingResponseDto> rankings) {
        this.rankings = rankings;
    }
}

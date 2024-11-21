package com.auction.domain.auction.dto.response.swagger;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "페이징된 경매 응답")
public class AuctionPageResponseDto {
    @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지당 항목 수", example = "10")
    private int size;

    @Schema(description = "전체 항목 수", example = "100")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;

    @Schema(description = "현재 페이지가 마지막 페이지인지 여부", example = "false")
    private boolean last;

    @Schema(description = "현재 페이지의 항목 목록")
    private List<AuctionResponseDto> content;
}

package com.auction.domain.auction.dto.response.swagger;

import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Schema(description = "조건 검색 결과 페이지 응답")
public class ItemDocumentResponsePageDto {
    @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지당 항목 수", example = "5")
    private int size;

    @Schema(description = "전체 항목 수", example = "50")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;

    @Schema(description = "현재 페이지가 마지막 페이지인지 여부", example = "false")
    private boolean last;

    @Schema(description = "검색 결과 항목 목록")
    private List<ItemDocumentResponseDto> content;

    public ItemDocumentResponsePageDto(Page<ItemDocumentResponseDto> page) {
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.content = page.getContent();
    }
}

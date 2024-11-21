package com.auction.domain.auction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCreateRequestDto {
    @Min(value = 1000, message = "최소 금액은 1000원 입니다.")
    @Schema(description = "최소 입찰 금액", example = "1000")
    private int minPrice;
    @Schema(description = "자동 연장 여부", example = "true")
    private boolean autoExtension;
    @DateTimeFormat(pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(description = "만료 시간", example = "00:10")
    private LocalTime expireAfter;
    @Schema(description = "물품 생성 요청 정보")
    private ItemCreateRequestDto item;
}

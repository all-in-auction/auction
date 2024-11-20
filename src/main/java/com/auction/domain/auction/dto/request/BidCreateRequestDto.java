package com.auction.domain.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "입찰 등록 요청 DTO")
public class BidCreateRequestDto {
    @NotNull(message = "입찰 금액은 필수입니다.")
    @Min(value = 1_000, message = "입찰 금액은 최소 1000원부터 시작입니다.")
    @Schema(description = "입찰 금액", example = "10000")
    private Integer price;
}

package com.auction.domain.auction.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionItemChangeRequestDto {
    private String name;
    private String description;
    @Min(value = 1000, message = "최소 금액은 1000원 입니다.")
    private String category;
}

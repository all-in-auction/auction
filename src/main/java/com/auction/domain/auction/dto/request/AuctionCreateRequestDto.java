package com.auction.domain.auction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class AuctionCreateRequestDto {
    @Min(value = 1000, message = "최소 금액은 1000원 입니다.")
    private int minPrice;
    private boolean autoExtension;
    @DateTimeFormat(pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime expireAfter;
    private ItemCreateRequestDto item;
}

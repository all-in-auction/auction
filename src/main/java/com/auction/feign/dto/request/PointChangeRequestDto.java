package com.auction.feign.dto.request;

import com.auction.feign.enums.PaymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointChangeRequestDto {
    private int amount;
    private PaymentType paymentType;

    public static PointChangeRequestDto of(int amount, PaymentType paymentType) {
        return new PointChangeRequestDto(amount, paymentType);
    }
}

package com.auction.feign.dto.request;

import com.auction.feign.enums.PaymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PointChangeRequestDto {
    private int amount;
    private PaymentType paymentType;

    public static PointChangeRequestDto of(int amount, PaymentType paymentType) {
        return new PointChangeRequestDto(amount, paymentType);
    }
}

package com.auction.feign.enums;

import com.auction.common.enums.Describable;
import lombok.Getter;

@Getter
public enum PaymentType implements Describable {
    CHARGE("충전"),
    SPEND("사용"),
    RECEIVE("지급"),
    TRANSFER("전환"),
    REFUND("환불");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }
}
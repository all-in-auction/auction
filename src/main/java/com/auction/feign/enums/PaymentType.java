package com.auction.feign.enums;

import lombok.Getter;

@Getter
public enum PaymentType {
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
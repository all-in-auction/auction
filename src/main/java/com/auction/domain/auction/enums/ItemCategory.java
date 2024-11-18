package com.auction.domain.auction.enums;

import com.auction.common.enums.Describable;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ItemCategory implements Describable {
    ELECTRONICS("전자기기"),
    HOME_APPLIANCES("가전제품"),
    FURNITURE("가구"),
    CLOTHING("의류"),
    BABY_PRODUCTS("유아 용품"),
    SPORTS_EQUIPMENT("스포츠 용품"),
    BOOKS("서적"),
    BEAUTY("뷰티"),
    TOYS("장난감"),
    MUSICAL_INSTRUMENTS("악기"),
    PET_SUPPLIES("반려동물 용품"),
    AUTOMOTIVE("차량 용품"),
    OFFICE_SUPPLIES("사무 용품"),
    COLLECTIBLES("수집품"),
    TOOLS("연장"),
    FOOD("식품"),
    GIFT_CERTIFICATES("상품권"),
    HEALTH("건강"),
    DIGITAL_CONTENT("디지털 콘텐츠");

    private final String description;

    public static ItemCategory of(String category) {
        return Arrays.stream(ItemCategory.values())
                .filter(c -> c.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestStateException("유효하지 않은 카테고리 입니다."));
    }

    ItemCategory(String description) {this.description = description;}
}

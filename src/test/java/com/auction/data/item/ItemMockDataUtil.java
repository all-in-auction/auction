package com.auction.data.item;

import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import org.springframework.test.util.ReflectionTestUtils;

public class ItemMockDataUtil {
    public static Item item() {
        Item item = Item.of("name", "description", ItemCategory.BOOKS);
        ReflectionTestUtils.setField(item, "id", 1L);
        return item;
    }
}

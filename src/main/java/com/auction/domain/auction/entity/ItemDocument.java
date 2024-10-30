package com.auction.domain.auction.entity;

import com.auction.domain.auction.enums.ItemCategory;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Document(indexName = "items")
public class ItemDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private ItemCategory category;

    public static ItemDocument from(Item item) {
        return new ItemDocument(item.getId(), item.getName(), item.getDescription(), item.getCategory());
    }
}

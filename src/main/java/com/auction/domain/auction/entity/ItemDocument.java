package com.auction.domain.auction.entity;

import com.auction.domain.auction.enums.ItemCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Document(indexName = "item_documents")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text)
    @JsonProperty("name")
    private String name;

    @Field(type = FieldType.Text)
    @JsonProperty("description")
    private String description;

    @Field(type = FieldType.Keyword)
    @JsonProperty("category")
    private ItemCategory category;

    public static ItemDocument from(Item item) {
        return new ItemDocument(item.getId(), item.getName(), item.getDescription(), item.getCategory());
    }
}

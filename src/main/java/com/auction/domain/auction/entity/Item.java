package com.auction.domain.auction.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.auction.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Entity
@NoArgsConstructor
@Document(indexName = "items")
public class Item extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @Field(type = FieldType.Text)
    private String name;

    @Column(length = 500)
    @Field(type = FieldType.Text)
    private String description;

    @Enumerated(EnumType.STRING)
    @Field(type = FieldType.Keyword)
    private ItemCategory category;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL)
    private Auction auction;

    private Item(String name, String description, ItemCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
    public static Item of(String name, String description, ItemCategory category) {
        return new Item(name,description,category);
    }
    public void changeName(String name) {this.name = name;}
    public void changeDescription(String description) {this.description = description;}
    public void changeCategory(ItemCategory category) {this.category = category;}
}

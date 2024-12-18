package com.auction.domain.auction.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Getter
@Entity
@Table(name = "auction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @NotNull
    @Column(name = "min_price")
    private int minPrice;

    @NotNull
    @Column(name = "max_price")
    private int maxPrice;

    @Column(name = "is_sold")
    private boolean isSold;

    @NotNull
    @Column(name = "is_auto_extension")
    private boolean isAutoExtension;

    @NotNull
    @Column(name = "expire_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireAt;

    private Auction(Item item, User seller, int minPrice, boolean isAutoExtension, LocalTime expireAfter) {
        this.item = item;
        this.seller = seller;
        this.minPrice = minPrice;
        this.maxPrice = minPrice;
        this.isAutoExtension = isAutoExtension;
        this.expireAt = LocalDateTime.now().plusHours(expireAfter.getHour()).plusMinutes(expireAfter.getMinute());
    }

    public static Auction of(Item item, User seller, int minPrice, boolean isAutoExtension, LocalTime expireAfter) {
        return new Auction(item, seller, minPrice, isAutoExtension, expireAfter);
    }

    public void changeItem(Item item) {
        this.item = item;
    }

    public void changeExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public void changeMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void changeBuyer(User buyer) {
        this.buyer = buyer;
        this.isSold = true;
    }

    public static Auction fromCache(Map<Object, Object> cache) {
        Auction auction = new Auction();
        auction.id = Long.parseLong((String) cache.get("id"));
        auction.seller = new User(Long.parseLong((String) cache.get("sellerId")));
        auction.minPrice = Integer.parseInt((String) cache.get("minPrice"));
        auction.maxPrice = Integer.parseInt((String) cache.get("maxPrice"));
        auction.isSold = Boolean.parseBoolean((String) cache.get("isSold"));
        auction.isAutoExtension = Boolean.parseBoolean((String) cache.get("autoExtension"));
        auction.expireAt = LocalDateTime.parse(cache.get("expireAt").toString());
        return auction;
    }


    public Long getBuyerId() {
        return buyer != null ? buyer.getId() : null;
    }
}
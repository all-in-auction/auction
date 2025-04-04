package com.auction.data.auction;

import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.auction.data.item.ItemMockDataUtil.item;

public class AuctionMockDataUtil {
    public static BidCreateRequestDto bidCreateRequestDto() {
        return new BidCreateRequestDto(1000000);
    }

    public static Auction auction() {
        Auction auction = Auction.of(item(), User.fromUserId(1L), 1000, false, LocalTime.now());
        ReflectionTestUtils.setField(auction, "id", 1L);
        ReflectionTestUtils.setField(auction, "isAutoExtension", true);
        return auction;
    }

    public static Auction expiredBefore5MinAuction() {
        Auction auction = Auction.of(item(), User.fromUserId(1L), 1000, false, LocalTime.now());
        ReflectionTestUtils.setField(auction, "id", 1L);
        ReflectionTestUtils.setField(auction, "expireAt", LocalDateTime.now().plusMinutes(3));
        ReflectionTestUtils.setField(auction, "isAutoExtension", true);
        return auction;
    }

    public static Auction expiredAuction() {
        Auction auction = Auction.of(item(), User.fromUserId(1L), 1000, false, LocalTime.now());
        ReflectionTestUtils.setField(auction, "id", 1L);
        ReflectionTestUtils.setField(auction, "expireAt", LocalDateTime.now().minusHours(3));
        return auction;
    }
}

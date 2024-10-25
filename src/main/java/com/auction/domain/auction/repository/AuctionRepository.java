package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.Auction;
import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionQueryRepository {
    @Query("SELECT a FROM Auction a JOIN FETCH Item i ON a.item.id = i.id WHERE a.id = ?1")
    Optional<Auction> findByAuctionId(long auctionId);

    Optional<Auction> findByIdAndSellerId(Long auctionId, Long sellerId);

    @Query("SELECT a.item.id FROM Auction a WHERE a.seller = :user")
    List<Long> findItemIdListBySeller(@Param("user") User user);

    List<Auction> findByBuyer(User buyer);
}

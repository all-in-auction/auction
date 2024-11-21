package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionItemImageRepository extends JpaRepository<AuctionItemImage, Long> {
}

package com.auction.domain.review.repository;

import com.auction.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByAuctionId(Long auctionId);
}
package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionQueryRepository {
    Page<AuctionResponseDto> findAllCustom(Pageable pageable);
    Page<AuctionResponseDto> findByKeyword(Pageable pageable, String keyword, String category, String sortBy);
}

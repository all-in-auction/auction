package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.ItemSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionQueryRepository {
    Page<AuctionResponseDto> findAllCustom(Pageable pageable);
    Page<ItemSearchResponseDto> findByKeyword(Pageable pageable, String keyword);
}

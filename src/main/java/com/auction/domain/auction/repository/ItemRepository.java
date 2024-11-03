package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.ItemSearchResponseDto;
import com.auction.domain.auction.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.id IN :itemIdList")
    List<Item> findByIdList(List<Long> itemIdList);

    @Query("SELECT new com.auction.domain.auction.dto.response.ItemSearchResponseDto(i.id, i.name, i.description) " +
            "FROM Item i WHERE i.name LIKE :keyword%")
    Page<ItemSearchResponseDto> findByKeyword(Pageable pageable, @Param("keyword") String keyword);
}

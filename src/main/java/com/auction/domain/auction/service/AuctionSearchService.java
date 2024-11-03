package com.auction.domain.auction.service;

import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import com.auction.domain.auction.dto.response.ItemSearchResponseDto;
import com.auction.domain.auction.elasticsearch.repository.ItemElasticRepository;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuctionSearchService {

    private final ItemRepository itemRepository;
    private final ItemElasticRepository elasticRepository;

    // ES 적용 X
    public Page<ItemSearchResponseDto> searchAuctionItemsByKeyword(Pageable pageable, String keyword) {
        return itemRepository.findByKeyword(pageable, keyword);
    }
    public Page<ItemSearchResponseDto> searchAllAuctionItems(Pageable pageable) {
        List<Item> all = itemRepository.findAll();
        List<ItemSearchResponseDto> dtos = all.stream().map(ItemSearchResponseDto::from).toList();
        return new PageImpl<>(dtos, pageable, all.size());
    }

    // ES 적용 O
    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByName(Pageable pageable, String name) {
        List<ItemDocument> documents = elasticRepository.findByName(name);
        List<ItemDocumentResponseDto> dtos = documents.stream().map(ItemDocumentResponseDto::from).toList();
        return new PageImpl<>(dtos, pageable, documents.size());
    }
    public Page<ItemDocumentResponseDto> elasticSearchAllAuctionItems(Pageable pageable) {
        List<ItemDocument> documents = (List<ItemDocument>) elasticRepository.findAll();
        List<ItemDocumentResponseDto> dtos = documents.stream().map(ItemDocumentResponseDto::from).toList();
        return new PageImpl<>(dtos, pageable, documents.size());
    }
}

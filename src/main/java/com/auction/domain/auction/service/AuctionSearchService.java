package com.auction.domain.auction.service;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import com.auction.domain.auction.elasticsearch.repository.ItemElasticRepository;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.MoreLikeThisQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

@RequiredArgsConstructor
@Service
public class AuctionSearchService {

    private final AuctionRepository auctionRepository;
    private final ItemElasticRepository elasticRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    // ES 적용 X
    public Page<AuctionResponseDto> searchAuctionItemsByKeyword(Pageable pageable, String keyword) {
        return auctionRepository.findByKeyword(pageable, keyword);
    }
    public Page<AuctionResponseDto> searchAllAuctionItems(Pageable pageable) {
        List<Auction> all = auctionRepository.findAll();
        List<AuctionResponseDto> dtos = all.stream().map(AuctionResponseDto::from).toList();
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

//    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByKeyword(Pageable pageable, String keyword) {
//        List<ItemDocument> documents = elasticRepository.findByKeyword(keyword);
//        List<ItemDocumentResponseDto> dtos = documents.stream().map(ItemDocumentResponseDto::from).toList();
//        return new PageImpl<>(dtos, pageable, documents.size());
//    }
}

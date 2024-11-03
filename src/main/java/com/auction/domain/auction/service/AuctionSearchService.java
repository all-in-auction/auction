package com.auction.domain.auction.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuctionSearchService {

    private final AuctionRepository auctionRepository;
    private final ElasticsearchClient elasticsearchClient;

    // 기본 키워드 검색 (Elasticsearch 적용 안했을 때)
//    public Page<AuctionResponseDto> searchAuctionItemsByKeyword(Pageable pageable, String keyword) {
//        return auctionRepository.findByKeyword(pageable, keyword);
//    }

    // name 필드로 검색 (Elasticsearch 적용)
    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByName(Pageable pageable, String keyword) throws IOException {
        Query query = MatchQuery.of(m -> m
                .field("name")
                .query(keyword)
        )._toQuery();

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("item_documents")
                .query(query)
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize())
        );

        SearchResponse<ItemDocument> response = elasticsearchClient.search(searchRequest, ItemDocument.class);

        List<ItemDocumentResponseDto> dtos = response.hits().hits().stream()
                .map(hit -> ItemDocumentResponseDto.from(hit.source()))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, response.hits().total().value());
    }

    // Elasticsearch에서 키워드와 필터를 사용하여 검색
    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByKeyword(Pageable pageable, String keyword) throws IOException {
        // BoolQueryBuilder를 사용하여 검색 조건을 생성
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        if (keyword != null && !keyword.isEmpty()) {
            boolQueryBuilder.should(MatchQuery.of(m -> m.field("name").query(keyword).boost(2.0F))._toQuery())
                    .should(MatchQuery.of(m -> m.field("description").query(keyword))._toQuery());
        }
//
//        if (category != null && !category.isEmpty()) {
//            boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("category").value(category))));
//        }

        Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));

        // 검색 요청 생성 및 실행 (정렬 없이 기본 정렬 사용)
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("item_documents")
                .query(query)
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize())
        );

        SearchResponse<ItemDocument> response = elasticsearchClient.search(searchRequest, ItemDocument.class);

        List<ItemDocumentResponseDto> dtos = response.hits().hits().stream()
                .map(hit -> ItemDocumentResponseDto.from(hit.source()))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, response.hits().total().value());
    }

}

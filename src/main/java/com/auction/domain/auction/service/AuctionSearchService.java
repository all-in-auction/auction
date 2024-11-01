package com.auction.domain.auction.service;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import com.auction.domain.auction.elasticsearch.repository.ItemElasticRepository;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByName(Pageable pageable, String keyword) {
        List<ItemDocument> documents = elasticRepository.findByName(keyword);
        List<ItemDocumentResponseDto> dtos = documents.stream().map(ItemDocumentResponseDto::from).toList();
        return new PageImpl<>(dtos, pageable, documents.size());
    }

    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByKeyword(Pageable pageable, String keyword, String category, String sortBy) {
        // 기본 쿼리 빌더
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//
//        // 키워드가 있을 경우 name과 description 필드에서 검색
//        if (keyword != null && !keyword.isEmpty()) {
//            queryBuilder.withQuery(
//                    boolQuery()
//                            .should(matchQuery("name", keyword).boost(2))  // name 필드에 대해 높은 가중치
//                            .should(matchQuery("description", keyword))   // description 필드에 대해 낮은 가중치
//            );
//        }
//
//        // 카테고리 필터 추가
//        if (category != null && !category.isEmpty()) {
//            queryBuilder.withFilter(termQuery("category", category));
//        }
//
//        // 정렬 조건 설정
//        if ("oldest".equals(sort)) {
//            queryBuilder.withSort(Sort.by("createdDate").ascending());
//        } else if ("priceLow".equals(sortBy)) {
//            queryBuilder.withSort(Sort.by("price").ascending());
//        } else if ("priceHigh".equals(sortBy)) {
//            queryBuilder.withSort(Sort.by("price").descending());
//        }
//
//        // 페이지 설정 추가
//        queryBuilder.withPageable(pageable);
//
//        // 최종 쿼리 작성
//        Query searchQuery = queryBuilder.build();
//
//        // 검색 실행 및 결과 수집
//        SearchHits<ItemDocument> searchHits = elasticsearchTemplate.search((MoreLikeThisQuery) searchQuery, ItemDocument.class);
//        List<ItemDocument> items = searchHits.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .collect(Collectors.toList());
//        List<ItemDocumentResponseDto> dtos = items.stream().map(ItemDocumentResponseDto::from).toList();

        // 검색 결과를 Page 객체로 변환하여 반환
//        return new PageImpl<>(dtos, pageable, searchHits.getTotalHits());
        return null;
    }
}

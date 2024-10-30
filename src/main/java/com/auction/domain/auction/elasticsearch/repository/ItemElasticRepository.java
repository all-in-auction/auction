package com.auction.domain.auction.elasticsearch.repository;

import com.auction.domain.auction.entity.ItemDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemElasticRepository extends ElasticsearchRepository<ItemDocument, Long> {
//    @Query("{"
//            + "  \"bool\": {"
//            + "    \"must\": ["
//            + "      { \"multi_match\": {"
//            + "          \"query\": \"?0\","
//            + "          \"fields\": [\"name\", \"description\"],"
//            + "          \"type\": \"best_fields\""
//            + "      }}"
//            + "    ],"
//            + "    \"filter\": ["
//            + "      { \"term\": { \"category\": \"?1\" } }"
//            + "    ]"
//            + "  }"
//            + "}")
//    List<ItemDocument> findByKeywordAndCategorySortBy(Pageable pageable, String keyword, String category, String sortBy);

    List<ItemDocument> findByName(String name);
}

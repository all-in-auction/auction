package com.auction.domain.auction.elasticsearch.repository;

import com.auction.domain.auction.entity.ItemDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemElasticRepository extends ElasticsearchRepository<ItemDocument, Long> {

    List<ItemDocument> findByName(String name);
}

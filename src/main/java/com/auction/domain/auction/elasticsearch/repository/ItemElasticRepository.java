package com.auction.domain.auction.elasticsearch.repository;

import com.auction.domain.auction.entity.ItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ItemElasticRepository extends ElasticsearchRepository<ItemDocument, Long> {
}

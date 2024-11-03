package com.auction.domain.auction.elasticsearch.repository;

import com.auction.domain.auction.entity.ItemDocument;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.io.IOException;
import java.util.List;

public interface ItemElasticRepository extends ElasticsearchRepository<ItemDocument,Long> {

    List<ItemDocument> findByName(String name) throws IOException;
    List<ItemDocument> findByCategoryAndName(String category, String name) throws IOException;

}

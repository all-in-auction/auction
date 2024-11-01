package com.auction.domain.auction.elasticsearch.repository;

import com.auction.domain.auction.entity.ItemDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemElasticRepository extends ElasticsearchRepository<ItemDocument, Long> {

//    @Query("""
//            {
//                "bool": {
//                  "must": [
//                    {
//                      "match": {
//                        "message": {
//                          "query": "#{@queryParameter.value}"
//                        }
//                      }
//                    }
//                  ],
//                  "should": [
//                    {
//                      "match_phrase": {
//                        "message": "#{@queryParameter.value}"
//                      }
//                    }
//                  ]
//            }"""
//    )
//    List<ItemDocument> findByKeyword(String name);

    @Query("{\"bool\": { \"must\": [ \n" +
            "    {\"wildcard\": {\"name\": \"*?0*\"}}]}}")
    List<ItemDocument> findByName(String name);
}

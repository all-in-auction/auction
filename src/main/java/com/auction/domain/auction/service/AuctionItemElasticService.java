package com.auction.domain.auction.service;

import com.auction.domain.auction.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionItemElasticService {
//    private final ItemElasticRepository itemElasticRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    @Async
    public void saveToElastic(Item item) {
        elasticsearchOperations.save(item);
    }

    @Async
    public void deleteFromElastic(Item item) {
        elasticsearchOperations.delete(item);
    }
}

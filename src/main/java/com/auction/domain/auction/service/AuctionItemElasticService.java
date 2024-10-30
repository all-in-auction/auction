package com.auction.domain.auction.service;

import com.auction.domain.auction.elasticsearch.repository.ItemElasticRepository;
import com.auction.domain.auction.entity.ItemDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionItemElasticService {

    private final ItemElasticRepository itemElasticRepository;

    @Async
    public void saveToElastic(ItemDocument item) {
        itemElasticRepository.save(item);
    }

    @Async
    public void deleteFromElastic(ItemDocument item) {
        itemElasticRepository.delete(item);
    }



}

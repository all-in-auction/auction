package com.auction;

import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.auction.service.AuctionSearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
@Commit
class AuctionApplicationTests {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private AuctionSearchService searchService;
    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    private final String jsonFilePath = "src/main/resources/used_items_dataset_unique.json";

    @Test
    public void insertData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 파일에서 데이터를 읽어 리스트로 변환
            List<Map<String, String>> itemsData = objectMapper.readValue(
                    new File(jsonFilePath), new TypeReference<List<Map<String, String>>>(){});

            int batchSize = 1000;

            List<Item> items = new ArrayList<>();
            List<ItemDocument> documents = new ArrayList<>();
            for (Map<String, String> itemData : itemsData) {
                Item item = Item.of(itemData.get("name"),
                        itemData.get("description"),
                        ItemCategory.of(itemData.get("itemCategory"))
                );
                items.add(item);
                ItemDocument document = ItemDocument.from(item);
                documents.add(document);

                // 배치 사이즈에 도달할 때마다 flush 및 clear
                if (items.size() % batchSize == 0) {
                    itemRepository.saveAllAndFlush(items);
                    elasticsearchRepository.saveAll(documents);
                    items.clear();
                    documents.clear();
                }
            }

            // 남은 데이터 저장
            if(!items.isEmpty()) {
                itemRepository.saveAllAndFlush(items);
                elasticsearchRepository.saveAll(documents);
            }
            System.out.println("Batch insert completed successfully!");

        } catch (IOException e) {
            System.err.println("Failed to load data: " + e.getMessage());
        }
    }

    @Test
    void get_search_time_test() {
        int count = 5;
        long sum = 0;
        String[] keyword = {"노트북", "케이스", "셔츠", "이어폰", "의자"};

        for (int i = 0; i < count; i++) {
            long start = System.currentTimeMillis();
            searchService.searchAuctionItemsByKeyword(PageRequest.of(0, 10), keyword[i]);
            long end = System.currentTimeMillis();
            sum += (end - start);
        }

        double avg = (double) sum / count;
        System.out.println("Average search time (ms): " + avg);
    }

    @Test
    void get_elasticsearch_time_test() {
        int count = 5;
        long sum = 0;
        String[] keyword = {"노트북", "케이스", "셔츠", "이어폰", "의자"};

        for (int i = 0; i < count; i++) {
            long start = System.currentTimeMillis();
            searchService.elasticSearchAuctionItemsByName(PageRequest.of(0, 10), keyword[i]);
            long end = System.currentTimeMillis();
            sum += (end - start);
        }

        double avg = (double) sum / count;
        System.out.println("Average search time (ms): " + avg);
    }

}

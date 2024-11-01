//package com.auction.domain.auction.repository;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import com.auction.domain.auction.elasticsearch.repository.ItemElasticRepository;
//import com.auction.domain.auction.entity.ItemDocument;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Repository
//@RequiredArgsConstructor
//public class ItemElasticCustomRepositoryImpl implements ItemElasticRepository {
//
//    private final ElasticsearchClient elasticsearchClient;
//
//    @Override
//    public List<ItemDocument> findByName(String name) throws IOException {
//        Query query = Query.of(q -> q.match(m -> m.field("name").query(name)));
//        SearchResponse<ItemDocument> response = elasticsearchClient.search(s -> s
//                .index("item_documents")
//                .query(query), ItemDocument.class);
//
//        return response.hits().hits().stream()
//                .map(hit -> hit.source())
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ItemDocument> findByCategoryAndName(String category, String name) throws IOException {
//        BoolQuery boolQuery = BoolQuery.of(b -> b
//                .must(Query.of(q -> q.term(t -> t.field("category").value(category))))
//                .must(Query.of(q -> q.match(m -> m.field("name").query(name))))
//        );
//
//        Query query = Query.of(q -> q.bool(boolQuery));
//        SearchResponse<ItemDocument> response = elasticsearchClient.search(s -> s
//                .index("item_documents")
//                .query(query), ItemDocument.class);
//
//        return response.hits().hits().stream()
//                .map(hit -> hit.source())
//                .collect(Collectors.toList());
//    }
//}

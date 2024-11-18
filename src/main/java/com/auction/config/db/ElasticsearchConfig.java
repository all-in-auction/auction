package com.auction.config.db;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    String url;

    @Bean
    public RestHighLevelClient client() {
        String host = extractHost(url);
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, 9200, "http"));
        return new RestHighLevelClient(builder);
    }

    private String extractHost(String url) {
        int colonIndex = url.indexOf(':'); // ':' 위치 확인 (포트 시작)
        if (colonIndex != -1) {
            return url.substring(0, colonIndex); // ':' 이전까지 추출
        }
        return url; // 포트나 경로가 없다면 전체 반환
    }
}

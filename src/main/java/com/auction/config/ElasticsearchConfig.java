package com.auction.config;

import com.amazonaws.ClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    String url;

    public ClientConfiguration clientConfiguration() {
        return (ClientConfiguration) org.springframework.data.elasticsearch.client.ClientConfiguration.builder()
                .connectedTo(url)
                .build();
    }
}

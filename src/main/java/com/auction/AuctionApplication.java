package com.auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
//@EnableJpaRepositories(excludeFilters = @ComponentScan.Filter(
//        type = FilterType.ASSIGNABLE_TYPE,
//        classes = ItemRepository.class))
@EnableElasticsearchRepositories(basePackages = "com.auction.domain.auction.elasticsearch.repository")
//@EnableDiscoveryClient
public class AuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionApplication.class, args);
    }

}

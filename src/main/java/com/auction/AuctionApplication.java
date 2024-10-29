package com.auction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
//@EnableElasticsearchRepositories(basePackages = "com.auction.domain.auction.elasticsearch.repository")
public class AuctionApplication {

    public static void main(String[] args) {
        final Logger LOGGER = LoggerFactory.getLogger(AuctionApplication.class);
        SpringApplication.run(AuctionApplication.class, args);
    }

}

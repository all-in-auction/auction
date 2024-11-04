package com.auction.config;

import jakarta.annotation.PostConstruct;

public class DataSourceInit {
    @PostConstruct
    public void init() {
        DataSourceContextHolder.setDataSourceType("MASTER");
    }
}

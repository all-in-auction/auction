package com.auction.config;

import com.auction.feign.decoder.ApiErrorDecoder;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableFeignClients(basePackages = "com.auction.feign.service")
public class FeignConfig {
    private final long connectTimeout = 1000L;
    private final long readTimeout = 1000L;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ApiErrorDecoder();
    }

    @Bean
    Request.Options options() {
        return new Request.Options(
                connectTimeout,
                TimeUnit.MILLISECONDS,
                readTimeout,
                TimeUnit.MILLISECONDS,
                false
        );
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(500L, TimeUnit.SECONDS.toMillis(5), 2);
    }
}
package com.auction.config.web;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(configurationCircuitBreaker());
    }

    private io.github.resilience4j.circuitbreaker.CircuitBreakerConfig configurationCircuitBreaker() {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(40)                               //실패율 임계값(백분율 단위)
                .waitDurationInOpenState(Duration.ofMillis(10000))      //Open -> half-open 으로 전환되기 전에 대기시간
                .permittedNumberOfCallsInHalfOpenState(3)               //half-open 시에 허용되는 호출 수
                .slidingWindowSize(10)                                  //호출 결과를 기록하는 데 사용되는 슬라이딩 윈도우 크기
                .recordExceptions(RuntimeException.class)               //실패로 기록되어 실패율이 증가하는 예외 목록
                .build();
    }
}

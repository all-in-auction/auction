package com.auction.config.web;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

@Slf4j
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config,
                                                         RegistryEventConsumer<CircuitBreaker> circuitBreakerConsumer) {
        return CircuitBreakerRegistry.of(config, circuitBreakerConsumer);
    }

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(40)                               // 실패율 임계값(백분율 단위)
                .waitDurationInOpenState(Duration.ofMillis(10000))      // Open -> half-open 으로 전환되기 전에 대기시간
                .permittedNumberOfCallsInHalfOpenState(3)               // half-open 시에 허용되는 호출 수
                .slidingWindowType(COUNT_BASED)                         // 최근 N번의 호출 기반으로 실패율 계산
                .slidingWindowSize(5)                                  // 호출 결과를 기록하는데 사용되는 슬라이딩 윈도우 크기
                .recordExceptions(RuntimeException.class)               // 실패로 기록되어 실패율이 증가하는 예외 목록
                .build();
    }

    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerConsumer() {
        return new RegistryEventConsumer<>() {

            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                CircuitBreaker circuitBreaker = entryAddedEvent.getAddedEntry();
                circuitBreaker.getEventPublisher()
                        .onFailureRateExceeded(event -> log.error("### 서킷 브레이커 {}에서 실패율 {}%를 달성했습니다. - {} ###",
                                event.getCircuitBreakerName(), event.getFailureRate(), event.getCreationTime()))
                        .onError(event -> log.error("### 서킷 브레이커 {}에서 오류가 발생했습니다. ###",
                                event.getCircuitBreakerName()))
                        .onStateTransition(
                                event -> log.warn("### 서킷 브레이커 {}의 상태가 {}에서 {}으로 변경되었습니다. - {} ###",
                                        event.getCircuitBreakerName(), event.getStateTransition().getFromState(),
                                        event.getStateTransition().getToState(), event.getCreationTime())
                        );
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
            }
        };
    }
}

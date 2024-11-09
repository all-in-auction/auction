package com.auction.config;

import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.coupon.dto.CouponClaimMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableTransactionManagement
public class KafkaConfig {

    @Value("${kafka.producer.bootstrap-servers}")
    private String kafkaServer;

    @Bean
    public ProducerFactory<String, CouponClaimMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, CouponClaimMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, RefundEvent> refundProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");

        // 모든 레플리카에서 전송 완료 시 ack
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // refund KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, RefundEvent> refundKafkaTemplate() {
        KafkaTemplate<String, RefundEvent> kafkaTemplate = new KafkaTemplate<>(refundProducerFactory());

        kafkaTemplate.setProducerListener(new ProducerListener<>() {

            @Override
            public void onError(ProducerRecord<String, RefundEvent> producerRecord, RecordMetadata recordMetadata, Exception exception) {
                System.err.println("Message sending failed: " + producerRecord.value());
                exception.printStackTrace();
            }
        });
        return new KafkaTemplate<>(refundProducerFactory());
    }

    // ConsumerFactory 빈 정의
    @Bean
    public ConsumerFactory<String, CouponClaimMessage> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "couponGroup");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        // 신뢰할 수 있는 패키지 설정
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.auction.domain.coupon.dto, com.auction.domain.auction.event.dto");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    // KafkaListenerContainerFactory 빈 정의
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CouponClaimMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CouponClaimMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // refund ConsumerFactory 빈 정의
    @Bean
    public ConsumerFactory<String, RefundEvent> refundConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "refundGroup");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        // 신뢰할 수 있는 패키지 설정
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.auction.domain.auction.event.dto, com.auction.domain.coupon.dto");
        // 커밋 수동으로 설정
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    // refund KafkaListenerContainerFactory 빈 정의
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundEvent> refundKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RefundEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(refundConsumerFactory());
        factory.setCommonErrorHandler(refundErrorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

    @Bean
    // Consumer 로직에서 예외 발생 시 재시도 로직 ErrorHandler
    public DefaultErrorHandler refundErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            log.error("[Error] topic = {}, key = {}, value = {}, error message = {}",
                    consumerRecord.topic(),
                    consumerRecord.key(),
                    consumerRecord.value(),
                    exception.getMessage());
        }, new FixedBackOff(1000L, 10)); // 1초 간격으로 최대 10번 재시도

        return errorHandler;
    }
}

package com.auction.config.messageQueue;

import com.auction.domain.coupon.dto.CouponClaimMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
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

    // 공통 ProducerFactory
    private <T> ProducerFactory<String, T> createProducerFactory(Class<T> valueType) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transactional-id");  // 원자성 위해 transaction id 추가

        // 모든 레플리카에서 전송 완료 시 ack
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // 공통 KafkaTemplate 빈
    private <T> KafkaTemplate<String, T> createKafkaTemplate(Class<T> valueType) {
        return new KafkaTemplate<>(createProducerFactory(valueType));
    }

    // 쿠폰용 KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, CouponClaimMessage> couponKafkaTemplate() {
        KafkaTemplate<String, CouponClaimMessage> kafkaTemplate = createKafkaTemplate(CouponClaimMessage.class);
        kafkaTemplate.setTransactionIdPrefix("coupon-");
        return kafkaTemplate;
    }

    // 공통 ConsumerFactory
    private <T> ConsumerFactory<String, T> createConsumerFactory(String groupId, Class<T> valueType) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.auction.domain.coupon.dto, com.auction.domain.auction.event.dto");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    // 공통 KafkaListenerContainerFactory 빈
    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createKafkaListenerContainerFactory(
            ConsumerFactory<String, T> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    // 쿠폰용 KafkaListenerContainerFactory 빈 정의
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CouponClaimMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CouponClaimMessage> factory = createKafkaListenerContainerFactory(createConsumerFactory("couponGroup", CouponClaimMessage.class));
        factory.setCommonErrorHandler(errorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    // Consumer 로직에서 예외 발생 시 재시도 로직 ErrorHandler
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler((consumerRecord, exception) -> {
            log.error("[Error] topic = {}, key = {}, value = {}, error message = {}",
                    consumerRecord.topic(),
                    consumerRecord.key(),
                    consumerRecord.value(),
                    exception.getMessage());
        }, new FixedBackOff(1000L, 10)); // 1초 간격으로 최대 10번 재시도
    }
}

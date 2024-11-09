package com.auction.config;

import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.coupon.dto.CouponClaimMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

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
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // 공통 KafkaTemplate 빈
    private <T> KafkaTemplate<String, T> createKafkaTemplate(Class<T> valueType) {
        return new KafkaTemplate<>(createProducerFactory(valueType));
    }

    // 쿠폰용 KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, CouponClaimMessage> couponKafkaTemplate() {
        return createKafkaTemplate(CouponClaimMessage.class);
    }

    // 환불용 KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, RefundEvent> refundKafkaTemplate() {
        return createKafkaTemplate(RefundEvent.class);
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
        return createKafkaListenerContainerFactory(createConsumerFactory("couponGroup", CouponClaimMessage.class));
    }

    // 환불용 KafkaListenerContainerFactory 빈 정의
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundEvent> refundKafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(createConsumerFactory("refundGroup", RefundEvent.class));
    }
}

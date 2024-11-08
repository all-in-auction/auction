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
    public ProducerFactory<String, RefundEvent> RefundProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // refund KafkaTemplate 빈 정의
    @Bean
    public KafkaTemplate<String, RefundEvent> refundKafkaTemplate() {
        return new KafkaTemplate<>(RefundProducerFactory());
    }

    // ConsumerFactory 빈 정의
    @Bean
    public ConsumerFactory<String, CouponClaimMessage> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "couponGroup");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.auction.domain.coupon.dto");

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
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.auction.domain.auction.dto");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    // refund KafkaListenerContainerFactory 빈 정의
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundEvent> refundKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RefundEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(refundConsumerFactory());
        return factory;
    }
}
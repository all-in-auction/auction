package com.auction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.auction.common.constants.RabbitMQConst.*;

@Configuration
public class RabbitMqConfig {
    @Bean
    public CustomExchange auctionExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(AUCTION_EXCHANGE, "x-delayed-message", true, false, arguments);
    }

    @Bean
    public Queue auctionQueue1() {
        return QueueBuilder.durable(queueNames[0])
                .deadLetterExchange(AUCTION_DLX)
                .build();
    }

    @Bean
    public Binding auctionBinding1(Queue auctionQueue1, CustomExchange auctionExchange) {
        return BindingBuilder
                .bind(auctionQueue1)
                .to(auctionExchange)
                .with(routingKeys[0])
                .noargs();
    }

    @Bean
    public Queue auctionQueue2() {
        return QueueBuilder.durable(queueNames[0])
                .deadLetterExchange(AUCTION_DLX)
                .build();
    }

    @Bean
    public Binding auctionBinding2(Queue auctionQueue2, CustomExchange auctionExchange) {
        return BindingBuilder
                .bind(auctionQueue2)
                .to(auctionExchange)
                .with(routingKeys[1])
                .noargs();
    }

    @Bean
    public Queue auctionQueue3() {
        return QueueBuilder.durable(queueNames[2])
                .deadLetterExchange(AUCTION_DLX)
                .build();
    }

    @Bean
    public Binding auctionBinding3(Queue auctionQueue3, CustomExchange auctionExchange) {
        return BindingBuilder
                .bind(auctionQueue3)
                .to(auctionExchange)
                .with(routingKeys[2])
                .noargs();
    }

    @Bean
    TopicExchange refundExchange() {
        return new TopicExchange("exchange.refund");
    }

    @Bean
    Queue refundQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-queue-mode", "lazy");
        return new Queue("refund.queue", false, false, false, arguments);
    }

    @Bean
    Binding refundBinding(TopicExchange refundExchange, Queue refundQueue) {
        return BindingBuilder
                .bind(refundQueue)
                .to(refundExchange)
                .with("refund.*");
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(AUCTION_DLQ).build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(AUCTION_DLX).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }
}

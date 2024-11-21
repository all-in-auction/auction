package com.auction.config.messageQueue;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public Declarables auctionQueuesAndBindings(CustomExchange auctionExchange) {
        List<Declarable> declarableList = new ArrayList<>();

        for (int i = 0; i < queueNames.length; i++) {
            Queue queue = QueueBuilder.durable(queueNames[i])
                    // 노드에 큐 분산
                    .withArgument("x-queue-master-locator", "min-masters")
                    .deadLetterExchange(AUCTION_DLX)
                    .build();
            declarableList.add(queue);

            Binding binding = BindingBuilder.bind(queue)
                    .to(auctionExchange)
                    .with(routingKeys[i])
                    .noargs();
            declarableList.add(binding);
        }

        return new Declarables(declarableList.toArray(new Declarable[0]));
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

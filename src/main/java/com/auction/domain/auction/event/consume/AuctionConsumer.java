package com.auction.domain.auction.event.consume;

import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.service.AuctionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.auction.common.constants.RabbitMQConst.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionConsumer {
    private final ObjectMapper objectMapper;
    private final AuctionService auctionService;

    @RabbitListener(queues = {AUCTION_QUEUE_1, AUCTION_QUEUE_2, AUCTION_QUEUE_3})
    public void auctionConsumer(String message) {
        try {
            log.info("AuctionEvent = {}", message);
            AuctionEvent auctionEvent = objectMapper.readValue(message, AuctionEvent.class);
            auctionService.closeAuction(auctionEvent);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
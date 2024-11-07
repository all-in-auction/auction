package com.auction.domain.auction.event.consume;

import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.auction.service.AuctionService;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAuctionConsumer {

    private final ObjectMapper objectMapper;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final DepositService depositService;

    @KafkaListener(topics = "refund-point-topic", groupId = "refund-group")
    public void refundConsumer(String message) {
        try {
            log.info("RefundEvent = {}", message);
            RefundEvent refundEvent = objectMapper.readValue(message, RefundEvent.class);
            depositService.deleteDeposit(refundEvent.getUserId(), refundEvent.getAuctionId());
            pointService.increasePoint(refundEvent.getUserId(), refundEvent.getDeposit());
            pointHistoryService.createPointHistory(User.fromUserId(refundEvent.getUserId()), refundEvent.getDeposit(), PaymentType.REFUND);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
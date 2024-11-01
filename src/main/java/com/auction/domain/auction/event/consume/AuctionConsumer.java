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
import org.springframework.stereotype.Component;

import static com.auction.common.constants.RabbitMQConst.AUCTION_QUEUE_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionConsumer {

    private final ObjectMapper objectMapper;
    private final AuctionService auctionService;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final DepositService depositService;

    @RabbitListener(queues = {AUCTION_QUEUE_PREFIX + 1, AUCTION_QUEUE_PREFIX + 2, AUCTION_QUEUE_PREFIX + 3})
    public void auctionConsumer(String message) {
        try {
            log.info("AuctionEvent = {}", message);
            AuctionEvent auctionEvent = objectMapper.readValue(message, AuctionEvent.class);
            auctionService.closeAuction(auctionEvent);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @RabbitListener(queues = "refund.queue")
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
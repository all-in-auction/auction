package com.auction.domain.auction.event.consume;

import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAuctionConsumer {

    private final DepositService depositService;

    @KafkaListener(topics = "refund-point-topic", groupId = "refund-group",
            containerFactory = "refundKafkaListenerContainerFactory")
    public void refundConsumer(RefundEvent refundEvent, Acknowledgment ack) {
        log.info("RefundEvent = {}", refundEvent);
        depositService.deleteDeposit(refundEvent.getUserId(), refundEvent.getAuctionId());
        log.info("Success point refund : {}", refundEvent.getUserId());

        // 예외 없을 시 커밋
        ack.acknowledge();
    }
}
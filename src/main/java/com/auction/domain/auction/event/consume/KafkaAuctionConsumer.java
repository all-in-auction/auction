package com.auction.domain.auction.event.consume;

import com.auction.Point;
import com.auction.PointServiceGrpc;
import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.auction.service.AuctionBidGrpcService;
import com.auction.domain.auction.service.AuctionService;
import com.auction.domain.auth.service.AuthService;
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
    private final PointServiceGrpc.PointServiceBlockingStub pointServiceStub;
    private final AuthService authService;
    private final AuctionService auctionService;
    private final AuctionBidGrpcService auctionBidGrpcService;

    @KafkaListener(topics = "refund-point-topic", groupId = "refund-group",
            containerFactory = "refundKafkaListenerContainerFactory")
    public void refundConsumer(RefundEvent refundEvent, Acknowledgment ack) {
        log.info("RefundEvent = {}", refundEvent);
        depositService.deleteDeposit(refundEvent.getUserId(), refundEvent.getAuctionId());

        auctionBidGrpcService.increasePoint(refundEvent.getUserId(), refundEvent.getDeposit());
//        pointService.increasePoint(refundEvent.getUserId(), refundEvent.getDeposit());

        log.info("Success point refund : {}", refundEvent.getUserId());

        auctionBidGrpcService.createPointHistory(refundEvent.getUserId(), refundEvent.getDeposit(), Point.PaymentType.REFUND);
//        pointHistoryService.createPointHistory(User.fromUserId(refundEvent.getUserId()), refundEvent.getDeposit(), PaymentType.REFUND);

        // 예외 없을 시 커밋
        ack.acknowledge();
    }
}
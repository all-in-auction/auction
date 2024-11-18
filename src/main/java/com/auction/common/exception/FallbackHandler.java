package com.auction.common.exception;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.event.dto.AuctionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FallbackHandler {

    public BidCreateResponseDto createBidFallback(long userId, long auctionId,
                                                  BidCreateResponseDto bidCreateResponseDto,
                                                  Throwable t) {
        log.error("Fallback for createBid: User={}, Auction={}, Error={}",
                userId, auctionId, t.getMessage(), t);

        throw new ApiException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    public void closeAuctionFallback(AuctionEvent auctionEvent, Throwable t) {
        log.error("Fallback for closeAuction: AuctionEvent={}, Error={}",
                auctionEvent.toString(), t.getMessage());
        throw new ApiException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    public int grpcUserPointFallback(long userId, Throwable t) {
        log.error("Fallback for grpcUserPoint: userId={}, Error={}", userId, t.getMessage());

        throw new ApiException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    public void grpcDecreasePointFallback(long userId, int amount, Throwable t) {
        log.error("Fallback triggered for grpcDecreasePoint. User ID: {}, Amount: {}, Error: {}",
                userId, amount, t.getMessage(), t);
        throw new ApiException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
}

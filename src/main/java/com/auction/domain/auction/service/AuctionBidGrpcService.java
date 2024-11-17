package com.auction.domain.auction.service;

import com.auction.Point;
import com.auction.PointServiceGrpc;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionBidGrpcService {

    private final PointServiceGrpc.PointServiceBlockingStub pointServiceStub;

    public int grpcUserPoint(long userId) {
        try {
            Point.GetPointsRequest grpcRequest = Point.GetPointsRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            Point.GetPointsResponse pointAmount = pointServiceStub.getPoints(grpcRequest);

            return pointAmount.getTotalPoint();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    public void grpcDecreasePoint(long userId, int amount) {
        try {
            Point.DecreasePointsRequest grpcRequest = Point.DecreasePointsRequest.newBuilder()
                    .setUserId(userId)
                    .setAmount(amount)
                    .build();

            Point.DecreasePointsResponse grpcResponse = pointServiceStub.decreasePoints(grpcRequest);

            if (grpcResponse.getStatus().equalsIgnoreCase("FAILED")) {
                throw new ApiException(ErrorStatus._INVALID_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    public void createPointHistory(long userId, int amount, Point.PaymentType paymentType) {
        try {
            Point.CreatePointHistoryRequest grpcRequest = Point.CreatePointHistoryRequest.newBuilder()
                    .setUserId(userId)
                    .setAmount(amount)
                    .setPaymentType(paymentType)
                    .build();

            Point.CreatePointHistoryResponse grpcResponse = pointServiceStub.createPointHistory(grpcRequest);

            if (grpcResponse.getStatus().equalsIgnoreCase("FAIL")) {
                throw new ApiException(ErrorStatus._INVALID_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    public void increasePoint(long userId, int amount) {
        try {
            Point.IncreasePointRequest grpcRequest = Point.IncreasePointRequest.newBuilder()
                    .setUserId(userId)
                    .setAmount(amount)
                    .build();

            Point.IncreasePointResponse grpcResponse = pointServiceStub.increasePoint(grpcRequest);

            if (grpcResponse.getStatus().equalsIgnoreCase("FAIL")) {
                throw new ApiException(ErrorStatus._INVALID_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }
}

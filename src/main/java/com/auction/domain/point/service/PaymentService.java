package com.auction.domain.point.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.point.entity.Payment;
import com.auction.domain.point.repository.PaymentRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void createPayment(String orderId, User user, int pointAmount,
                              int paymentAmount, CouponUser couponUser) {
        Payment payment = Payment.of(orderId, user, pointAmount, paymentAmount, couponUser);
        paymentRepository.save(payment);
    }

    public void validateAmount(int amount) {
        if (amount < 1000 || amount % 1000 != 0) {
            throw new ApiException(ErrorStatus._INVALID_AMOUNT_REQUEST);
        }
    }

    public Payment getPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() -> new ApiException(ErrorStatus._INVALID_REQUEST));
    }
}

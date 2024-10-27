package com.auction.domain.deposit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepositService {
    private final RedisTemplate<String, Object> redisTemplate;

    private final String DEPOSIT = "deposit";

    private String key(long userId, long auctionId) {
        return "auctionId:" + auctionId + ":userId:" + userId;
    }

    public void placeDeposit(long userId, long auctionId, int price) {
        redisTemplate.opsForHash().put(key(userId, auctionId), DEPOSIT, String.valueOf(price));
    }

    public Optional<Object> getDeposit(long userId, long auctionId) {
        Object deposit = redisTemplate.opsForHash().get(key(userId, auctionId), DEPOSIT);
        return Optional.ofNullable(deposit);
    }

    public void deleteDeposit(long userId, long auctionId) {
        redisTemplate.opsForHash().delete(key(userId, auctionId), DEPOSIT);
    }
}

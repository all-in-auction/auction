package com.auction.domain.auction.service;

import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private AuctionPublisher auctionPublisher;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PointService pointService;
    @Mock
    private PointHistoryService pointHistoryService;
    @Mock
    private DepositService depositService;
    @Mock
    private UserService userService;
    @InjectMocks
    private AuctionService auctionService;

    @Test
    public void 연장된_경매() {
        // given
        long auctionId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataSourceExpiredAt = now.plusDays(1);
        long originExpiredAt = TimeConverter.toLong(now);

        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        Auction auction = mock(Auction.class);
        ReflectionTestUtils.setField(auction, "id", auctionId);

        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        when(auction.getExpireAt()).thenReturn(dataSourceExpiredAt);
        when(auctionEvent.getExpiredAt()).thenReturn(originExpiredAt);

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(auctionPublisher).auctionPublisher(eq(auctionEvent), eq(originExpiredAt), eq(TimeConverter.toLong(dataSourceExpiredAt)));
    }

    @Test
    public void 경매_유찰() {
        // given
        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        long auctionId = 1L;
        when(auctionEvent.getAuctionId()).thenReturn(auctionId);
        Auction auction = mock(Auction.class);
        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        when(auction.getExpireAt()).thenReturn(expireAt);
        when(auctionEvent.getExpiredAt()).thenReturn(TimeConverter.toLong(expireAt));
        when(auction.getSeller()).thenReturn(new User());

        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForZSet().reverseRange(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptySet());

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(notificationService).sendNotification(
                any(User.class), eq(NotificationType.AUCTION),
                eq("경매 아이디 " + auctionId + "이(가) 유찰되었습니다."), anyString()
        );
    }

    @Test
    public void 경매_낙찰() {
        // given
        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        long auctionId = 1L;
        when(auctionEvent.getAuctionId()).thenReturn(auctionId);
        Auction auction = mock(Auction.class);
        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        when(auction.getExpireAt()).thenReturn(expireAt);
        when(auctionEvent.getExpiredAt()).thenReturn(TimeConverter.toLong(expireAt));

        User buyer = new User();
        Long buyerId = 2L;
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForZSet().reverseRange("auction:bid:1", 0L, 0L)).thenReturn(Set.of(buyerId.toString()));

        User seller = new User();
        Long sellerId = 3L;
        ReflectionTestUtils.setField(seller, "id", sellerId);
        int maxPrice = 10000;
        when(auction.getSeller()).thenReturn(seller);
        when(auction.getMaxPrice()).thenReturn(maxPrice);
        when(userService.getUser(buyerId)).thenReturn(buyer);

        Item item = new Item();
        ReflectionTestUtils.setField(item, "name", "itemName");
        when(auction.getItem()).thenReturn(item);

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(pointService).increasePoint(sellerId, maxPrice);
        verify(pointHistoryService).createPointHistory(seller, maxPrice, PaymentType.RECEIVE);
        verify(auction).changeBuyer(buyer);
        verify(depositService).deleteDeposit(buyerId, auctionId);
        verify(notificationService).sendNotification(eq(buyer), eq(NotificationType.AUCTION), anyString(), anyString());
    }

}
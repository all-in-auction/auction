package com.auction.domain.auction.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.dto.request.AuctionCacheDto;
import com.auction.domain.auction.dto.response.AuctionCacheResponseDto;
import com.auction.domain.auction.dto.response.AuctionRankingResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuctionQueryService {
    private final AuctionRepository auctionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String AUCTION_CACHE_KEY_PREFIX = "auction:";
    private static final String AUCTION_RANKING_PREFIX = "auction:ranking:";

    @Transactional(readOnly = true)
    public AuctionCacheResponseDto getAuction(Long auctionId) {
        String cacheKey = AUCTION_CACHE_KEY_PREFIX + auctionId;
        AuctionCacheDto cachedAuction = (AuctionCacheDto) redisTemplate.opsForValue().get(cacheKey);

        if (cachedAuction == null) {
            Auction auction = auctionRepository.findByAuctionId(auctionId)
                    .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION));

            cachedAuction = AuctionCacheDto.from(auction);
            redisTemplate.opsForValue().set(cacheKey, cachedAuction);
        }

        return AuctionCacheResponseDto.of(cachedAuction);
    }

    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> getAuctionList(Pageable pageable) {
        return auctionRepository.findAllCustom(pageable);
    }

    @Transactional(readOnly = true)
    public List<AuctionRankingResponseDto> getRankingList() {
        Set<ZSetOperations.TypedTuple<Object>> rankings =
                redisTemplate.opsForZSet().reverseRangeWithScores(AUCTION_RANKING_PREFIX, 0, 9);

        List<AuctionRankingResponseDto> rankingList = new ArrayList<>();

        if (rankings != null) {
            int rank = 1;
            for (ZSetOperations.TypedTuple<Object> ranking : rankings) {
                long auctionId = Long.parseLong(ranking.getValue().toString());
                Auction auction = auctionRepository.findByAuctionId(auctionId)
                        .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION));

                Integer bidCount = ranking.getScore().intValue();
                rankingList.add(AuctionRankingResponseDto.of(rank++, auctionId, bidCount,
                        auction.getItem().getName(), auction.getMaxPrice(),
                        auction.getExpireAt().toString()));
            }
        }
        return rankingList;
    }

    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> searchAuctionItems(Pageable pageable, String name, String category, String sortBy) {
        return auctionRepository.findByCustomSearch(pageable, name, category, sortBy);
    }
}

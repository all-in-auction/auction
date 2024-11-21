package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.ItemSearchResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.auction.domain.auction.entity.QAuction.auction;
import static com.auction.domain.auction.entity.QItem.item;

public class AuctionQueryRepositoryImpl implements AuctionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AuctionQueryRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AuctionResponseDto> findAllCustom(Pageable pageable) {
        List<Auction> auctionList = queryFactory
                .selectFrom(auction)
                .leftJoin(auction.item, item).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(auction.count())
                .from(auction)
                .fetchOne()).orElse(0L);

        List<AuctionResponseDto> auctionResponseDtoList = auctionList.stream()
                .map(AuctionResponseDto::from)
                .toList();

        return new PageImpl<>(auctionResponseDtoList, pageable, total);
    }

    @Override
    public Page<ItemSearchResponseDto> findByKeyword(Pageable pageable, String keyword) {
        List<Item> itemList = queryFactory
                .selectFrom(item)
                .where(nameEq(keyword), descriptionHas(keyword))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(item.count())
                .from(item)
                .fetchOne()).orElse(0L);

        List<ItemSearchResponseDto> dtoList = itemList.stream()
                .map(ItemSearchResponseDto::from)
                .toList();

        return new PageImpl<>(dtoList, pageable, total);
    }

    private BooleanExpression nameEq(String keyword) {
        return keyword != null ? auction.item.name.contains(keyword) : null;
    }

    private BooleanExpression descriptionHas(String keyword) {
        return keyword != null ? auction.item.description.contains(keyword) : null;
    }
}

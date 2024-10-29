package com.auction.domain.review.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.review.dto.request.ReviewCreateRequestDto;
import com.auction.domain.review.dto.request.ReviewUpdateRequestDto;
import com.auction.domain.review.dto.response.ReviewResponseDto;
import com.auction.domain.review.entity.Review;
import com.auction.domain.review.repository.ReviewRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final AuctionRepository auctionRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewResponseDto createReview(AuthUser authUser, Long auctionId, ReviewCreateRequestDto requestDto) {
        Auction auction = auctionRepository.findByAuctionId(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION)
        );

        if (!auction.isSold()) {
            throw new ApiException(ErrorStatus._INVALID_REQUEST_REVIEW);
        }

        if (!Objects.equals(auction.getBuyerId(), authUser.getId())) {
            throw new ApiException(ErrorStatus._PERMISSION_DENIED);
        }

        Review review = new Review(auction, User.fromAuthUser(authUser), requestDto);
        reviewRepository.save(review);

        return ReviewResponseDto.of(review);
    }

    @Transactional
    public ReviewResponseDto updateReview(AuthUser authUser, Long auctionId, ReviewUpdateRequestDto requestDto) {
        Auction auction = auctionRepository.findByAuctionId(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION)
        );

        Review review = reviewRepository.findByAuctionId(auction.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_REVIEW)
        );

        if (!review.getUser().getId().equals(authUser.getId())) {
            throw new ApiException(ErrorStatus._PERMISSION_DENIED);
        }

        review.updateReview(requestDto);

        return ReviewResponseDto.of(review);
    }

    public ReviewResponseDto getReview(Long auctionId) {
        Review review = reviewRepository.findByAuctionId(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_REVIEW)
        );
        return ReviewResponseDto.of(review);
    }

    @Transactional
    public void deleteReview(AuthUser authUser, Long auctionId) {
        Review review = reviewRepository.findByAuctionId(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_REVIEW)
        );

        if (!authUser.getId().equals(review.getUser().getId())) {
            throw new ApiException(ErrorStatus._PERMISSION_DENIED);
        }

        reviewRepository.delete(review);
    }
}

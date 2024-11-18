package com.auction.domain.review.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.review.dto.request.ReviewCreateRequestDto;
import com.auction.domain.review.dto.request.ReviewUpdateRequestDto;
import com.auction.domain.review.dto.response.ReviewResponseDto;
import com.auction.domain.review.entity.Review;
import com.auction.domain.review.repository.ReviewRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDto createReview(Long userId, Long auctionId, ReviewCreateRequestDto requestDto) {
        Auction auction = auctionRepository.findByAuctionId(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION)
        );

        if (!auction.isSold()) {
            throw new ApiException(ErrorStatus._INVALID_REQUEST_REVIEW);
        }

        if (!Objects.equals(auction.getBuyerId(), userId)) {
            throw new ApiException(ErrorStatus._PERMISSION_DENIED);
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_USER)
        );

        Review review = new Review(auction, user, requestDto);
        reviewRepository.save(review);

        return ReviewResponseDto.of(review);
    }

    @Transactional
    public ReviewResponseDto updateReview(Long userId, Long auctionId, ReviewUpdateRequestDto requestDto) {
        Auction auction = auctionRepository.findByAuctionId(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION)
        );

        Review review = reviewRepository.findByAuctionId(auction.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_REVIEW)
        );

        if (!review.getUser().getId().equals(userId)) {
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
    public void deleteReview(Long userId, Long auctionId) {
        Review review = reviewRepository.findByAuctionId(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_REVIEW)
        );

        if (!userId.equals(review.getUser().getId())) {
            throw new ApiException(ErrorStatus._PERMISSION_DENIED);
        }

        reviewRepository.delete(review);
    }
}

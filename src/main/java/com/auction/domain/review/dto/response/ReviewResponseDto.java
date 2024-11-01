package com.auction.domain.review.dto.response;

import com.auction.domain.review.entity.Review;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReviewResponseDto {

    private final String title;
    private final String content;
    private final int star;

    public static ReviewResponseDto of(Review review) {
        return new ReviewResponseDto(
                review.getTitle(),
                review.getContent(),
                review.getStar()
        );
    }
}

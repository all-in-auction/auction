package com.auction.domain.review.dto.response;

import com.auction.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private String title;
    private String content;
    private int star;

    public static ReviewResponseDto of(Review review) {
        return new ReviewResponseDto(
                review.getTitle(),
                review.getContent(),
                review.getStar()
        );
    }
}

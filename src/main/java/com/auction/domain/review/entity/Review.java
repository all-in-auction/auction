package com.auction.domain.review.entity;

import com.auction.domain.auction.entity.Auction;
import com.auction.domain.review.dto.request.ReviewCreateRequestDto;
import com.auction.domain.review.dto.request.ReviewUpdateRequestDto;
import com.auction.domain.review.dto.response.ReviewResponseDto;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionId")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private String title;
    private String content;
    private int star;

    public Review(Auction auction, User user, ReviewCreateRequestDto requestDto) {
        this.auction = auction;
        this.user = user;
        this.title = requestDto.getTitle();
        this.content = requestDto.getContent();
        this.star = requestDto.getStar();
    }

    public void updateReview(ReviewUpdateRequestDto requestDto) {
        if (requestDto.getTitle() != null) this.title = requestDto.getTitle();
        if (requestDto.getContent() != null) this.content = requestDto.getContent();
    }
}

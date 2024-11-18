package com.auction.domain.coupon.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CouponCreateRequestDto {
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "쿠폰의 유효기간은 과거일 수 없습니다. 현재 시간 이후로 설정해 주세요.")
    private LocalDate expireAt;
    @Positive(message = "쿠폰의 수량은 0 또는 음수일 수 없습니다. 양수를 입력해주세요.")
    private Integer amount;
    @NotBlank(message = "쿠폰의 이름은 공백일 수 없습니다.")
    private String name;
    @NotNull
    @Range(min = 1, max = 100, message = "할인율이 유효하지 않습니다. 1에서 100 사이의 값을 입력해 주세요.")
    private int discountRate;
}

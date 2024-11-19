package com.auction.common.apipayload.status;

import com.auction.common.apipayload.BaseCode;
import com.auction.common.apipayload.dto.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {

    // common
    _INVALID_REQUEST(HttpStatus.BAD_REQUEST, "400", "잘못된 요청입니다."),
    _PERMISSION_DENIED(HttpStatus.FORBIDDEN, "403", "권한이 없습니다."),
    _TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "408", "잠시 후 다시 시도해주세요."),
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "요청에 실패하였습니다."),

    // auth
    _NOT_AUTHENTICATIONPRINCIPAL_USER(HttpStatus.UNAUTHORIZED, "401", "인증되지 않은 유저입니다."),
    _NOT_FOUND_USER(HttpStatus.NOT_FOUND, "404", "권한이 없습니다."),

    // auction item
    _NOT_FOUND_AUCTION_ITEM(HttpStatus.NOT_FOUND, "404", "해당 경매 물품을 찾을 수 없습니다."),

    // pay
    _INVALID_AMOUNT_REQUEST(HttpStatus.BAD_REQUEST, "400", "결제 금액은 1000원 단위입니다."),
    _INVALID_PAY_REQUEST(HttpStatus.BAD_REQUEST, "400", "잘못된 거래 승인 요청입니다."),
    _INVALID_CONVERT_REQUEST(HttpStatus.BAD_REQUEST, "400", "현재 포인트 잔고보다 더 큰 값을 전환 요청할 수 없습니다."),

    // auction image
    _INVALID_IMAGE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "415", "지원하지 않는 파일 형식입니다."),
    _NOT_FOUND_AUCTION_ITEM_IMAGE(HttpStatus.NOT_FOUND, "404", "해당 경매 물품 사진을 찾을 수 없습니다."),

    // auction
    _NOT_FOUND_AUCTION(HttpStatus.NOT_FOUND, "404", "해당 경매를 찾을 수 없습니다."),
    _INVALID_BID_CLOSED_AUCTION(HttpStatus.BAD_REQUEST, "400", "이미 종료된 경매입니다."),
    _INVALID_BID_REQUEST_USER(HttpStatus.BAD_REQUEST, "400", "경매 등록자는 경매에 참여할 수 없습니다."),
    _INVALID_NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "400", "포인트 충전 후 다시 시도해주세요."),
    _INVALID_LESS_THAN_MAX_PRICE(HttpStatus.BAD_REQUEST, "400", "입찰가는 최고 입찰가보다 높아야 합니다."),

    // coupon
    _NOT_FOUND_COUPON(HttpStatus.NOT_FOUND, "404", "해당 쿠폰을 찾을 수 없습니다."),
    _NOT_OWNED_COUPON(HttpStatus.BAD_REQUEST, "400", "사용자에게 해당 쿠폰이 없습니다."),
    _ALREADY_USED_COUPON(HttpStatus.BAD_REQUEST, "400", "이미 사용된 쿠폰입니다."),
    _EXPIRED_COUPON(HttpStatus.BAD_REQUEST, "400", "유효기간이 지난 쿠폰입니다."),
    _SOLD_OUT_COUPON(HttpStatus.CONFLICT, "409", "준비된 쿠폰 수량이 모두 소진되었습니다."),
    _ALREADY_CLAIMED_COUPON(HttpStatus.BAD_REQUEST, "400", "이미 수령한 쿠폰입니다."),
    _INTERNAL_SERVER_ERROR_COUPON(HttpStatus.INTERNAL_SERVER_ERROR, "500", "쿠폰 발급에 실패하였습니다."),


    // review
    _NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "404", "해당 리뷰를 찾을 수 없습니다."),
    _INVALID_REQUEST_REVIEW(HttpStatus.NOT_FOUND, "400", "종료된 경매만 리뷰를 작성할 수 있습니다.");

    private HttpStatus httpStatus;
    private String statusCode;
    private String message;

    public static ErrorStatus getErrorStatus(String findMessage) {
        return Arrays.stream(values())
                .filter(errorStatus -> errorStatus.message.equals(findMessage))
                .findAny()
                .orElse(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .statusCode(statusCode)
                .message(message)
                .httpStatus(httpStatus)
                .success(false)
                .build();
    }
}

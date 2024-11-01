package com.auction.common.aop;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestLogDto {
    private String url;
    private String method;
    private Long requestUserId;
    private String exception;

    public static RequestLogDto of(String url, String method, Long requestUserId, String exception) {
        return new RequestLogDto(url, method, requestUserId, exception);
    }

    public void changeRequestUserId(long requestUserId) {
        this.requestUserId = requestUserId;
    }

    public void changeException(String exception) {
        this.exception = exception;
    }
}

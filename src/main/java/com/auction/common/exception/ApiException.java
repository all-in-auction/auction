package com.auction.common.exception;

import com.auction.common.apipayload.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ApiException extends RuntimeException {

    private BaseCode errorCode;

    public ApiException(BaseCode errorCode) {
        this.errorCode = errorCode;
        log.error("Exception occurred = {}", errorCode.getReasonHttpStatus().getMessage());
    }
}

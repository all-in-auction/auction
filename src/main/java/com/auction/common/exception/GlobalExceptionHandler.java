package com.auction.common.exception;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.apipayload.BaseCode;
import com.auction.common.apipayload.dto.ReasonDto;
import com.auction.common.apipayload.status.ErrorStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException e) {
//        return ResponseEntity.status(ErrorStatus._NOT_AUTHENTICATIONPRINCIPAL_USER.getHttpStatus())
//                .body(ApiResponse.fail(ErrorStatus._NOT_AUTHENTICATIONPRINCIPAL_USER));
//    }


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(ApiException e) {
        BaseCode errorCode = e.getErrorCode();
        return handleExceptionInternal(errorCode);
    }

    private ResponseEntity<ApiResponse<String>> handleExceptionInternal(BaseCode errorCode) {
        return ResponseEntity.status(errorCode.getReasonHttpStatus().getHttpStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleBindException(MethodArgumentNotValidException ex) {
        String errorCodes = ex.getBindingResult().getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(","));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "400", errorCodes, null));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<String>> handleFeignServerException(FeignException ex) {
        HttpStatus status;
        try {
            status = HttpStatus.valueOf(ex.status());
        } catch (IllegalArgumentException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity
                .status(HttpStatus.valueOf(status.value()))
                .body(new ApiResponse<>(
                                false,
                                String.valueOf(status.value()),
                                getErrorStatus(ex.getMessage()).getMessage(),
                                null
                        )
                );
    }

    private ErrorStatus getErrorStatus(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ReasonDto reasonDto = objectMapper.readValue(responseBody, ReasonDto.class);
            return ErrorStatus.getErrorStatus(reasonDto.getMessage());
        } catch (JsonProcessingException e) {
            return ErrorStatus._INTERNAL_SERVER_ERROR;
        }
    }

}

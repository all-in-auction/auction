package com.auction.common.apipayload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReasonDto {

    private String statusCode;
    private String message;
    private HttpStatus httpStatus;
    private Boolean success;
}
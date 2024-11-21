package com.auction.common.apipayload;

import com.auction.common.apipayload.status.SuccessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"success", "statusCode", "message", "data"})
public class ApiResponse<T> implements Serializable {

    @JsonProperty("success")
    private Boolean success;

    private String statusCode;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, SuccessStatus._OK.getStatusCode(), SuccessStatus._OK.getMessage(), data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, SuccessStatus._CREATED.getStatusCode(), SuccessStatus._CREATED.getMessage(), data);
    }

    public static ApiResponse<String> fail(BaseCode errorCode) {
        return new ApiResponse<>(false, errorCode.getReasonHttpStatus().getStatusCode(), errorCode.getReasonHttpStatus().getMessage(), null);
    }
}

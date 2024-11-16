package com.auction.feign.decoder;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.feign.utils.FeignClientUtil;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;

import static com.auction.common.apipayload.status.ErrorStatus.getErrorStatus;

import static java.lang.String.format;

@Slf4j
public class ApiErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        String responseBody = FeignClientUtil.getResponseBody(response);
        log.error("{} 요청이 성공하지 못했습니다. status: {} requestUrl: {}, requestBody: {}, responseBody: {}",
                response.status(), methodKey, response.request().url(), FeignClientUtil.getRequestBody(response), responseBody);

        if (isRetry(response)) {
            return new RetryableException(
                    response.status(),
                    format("%s", responseBody),
                    response.request().httpMethod(),
                    1000L,
                    response.request()
            );
        }
        return new ApiException(getErrorStatus(responseBody));
    }

    /**
     * 4XX, 5XX 에러이면서, GET 요청에 대해서만 retry
     */
    private boolean isRetry(Response response) {
        if (response.request().httpMethod() != Request.HttpMethod.GET) {
            return false;
        }

        return HttpStatusCode.valueOf(response.status()).is5xxServerError();
    }

}
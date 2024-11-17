package com.auction.feign.utils;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

@Slf4j
public class FeignClientUtil {
    public static String getRequestBody(Response response) {
        if (response.request().body() == null) {
            return "";
        }

        return new String(response.request().body(), StandardCharsets.UTF_8);
    }

    public static String getResponseBody(Response response) {
        if (response.body() == null) {
            return "";
        }

        try (InputStream responseBodyStream = response.body().asInputStream()) {
            return IOUtils.toString(responseBodyStream, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error(format("feign response body converting error - response: %s", response), e);
            return "";
        }
    }
}
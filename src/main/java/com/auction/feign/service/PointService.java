package com.auction.feign.service;

import com.auction.common.apipayload.ApiResponse;
import com.auction.config.FeignConfig;
import com.auction.feign.dto.request.PointChangeRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.auction.common.constants.Const.USER_ID;

@FeignClient(
        name = "point-service",
        url = "${spring.cloud.openfeign.url}",
        configuration = FeignConfig.class
)
public interface PointService {
    @PostMapping("/v4/points")
    ApiResponse<Void> createPoint(@RequestHeader(USER_ID) long userId);

    @PatchMapping("/v4/points")
    ApiResponse<Void> changePoint(
            @RequestHeader(USER_ID) long userId,
            @RequestBody PointChangeRequestDto pointChangeRequestDto
    );
}
package com.auction.config.web;

import com.auction.PointServiceGrpc;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {

    private final EurekaClient eurekaClient;

    @Bean
    public ManagedChannel pointServiceChannel() {
        // Eureka에서 서비스 이름으로 gRPC 서버 정보 검색
        InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka("points-service", false);

        // gRPC 서버의 IP와 포트 가져오기
        String grpcHost = instanceInfo.getIPAddr();

        // gRPC 서버 주소와 포트를 설정
        return ManagedChannelBuilder.forAddress(grpcHost, 8085)
                .usePlaintext()
                .build();
    }

    @Bean
    public PointServiceGrpc.PointServiceBlockingStub pointServiceStub(ManagedChannel pointServiceChannel) {
        return PointServiceGrpc.newBlockingStub(pointServiceChannel);
    }
}

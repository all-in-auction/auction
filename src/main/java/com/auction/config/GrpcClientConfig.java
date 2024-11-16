package com.auction.config;

import com.auction.PointServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    @Bean
    public ManagedChannel pointServiceChannel() {
        // gRPC 서버 주소와 포트를 설정
        return ManagedChannelBuilder.forAddress("127.0.0.1", 8085)
                .usePlaintext()
                .build();
    }

    @Bean
    public PointServiceGrpc.PointServiceBlockingStub pointServiceStub(ManagedChannel pointServiceChannel) {
        return PointServiceGrpc.newBlockingStub(pointServiceChannel);
    }
}

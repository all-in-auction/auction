package com.auction.config.web;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Configuration
@Slf4j
@Profile("prod")
public class EcsConfig {

    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfig(InetUtils inetUtils){

        EurekaInstanceConfigBean config = new EurekaInstanceConfigBean(inetUtils);

        config.setPreferIpAddress(true);
        config.setAppname("auction-service");
        String taskArn = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
        String ip = getPrivateIp(taskArn);
        config.setIpAddress(ip);
        config.setInstanceId("auction-service:"+ip);
        config.setNonSecurePort(8080);

        return config;
    }

    public static String getPrivateIp(String metadataUrl) {
        try {
            // 메타데이터 API 요청
            URL url = new URL(metadataUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 응답 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode metadata = objectMapper.readTree(response.toString());

            return metadata.get("Networks").get(0).get("IPv4Addresses").get(0).asText();
        } catch (Exception e) {
            log.info("Failed to fetch private IP from metadata", e);
            return null;
        }
    }
}

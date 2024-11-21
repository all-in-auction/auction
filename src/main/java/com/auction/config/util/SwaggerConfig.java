package com.auction.config.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://www.all-in-auction.site");
        gatewayServer.setDescription("Gateway Server");

        return new OpenAPI()
                .addServersItem(gatewayServer);
    }
}

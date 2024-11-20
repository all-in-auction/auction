package com.auction.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/templates/", "classpath:/static/")
                .setCacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v3/api-docs")
                .allowedOrigins("http://www.all-in-auction.site")
                .allowedMethods("GET");
        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("http://www.all-in-auction.site")
                .allowedMethods("GET");
    }
}

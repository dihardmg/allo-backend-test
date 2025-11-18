package com.home.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public WebClient frankfurterWebClient(FrankfurterApiProperties properties) {
        WebClientFactoryBean factoryBean = new WebClientFactoryBean(properties.getBaseUrl(), properties.getTimeout());
        try {
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create WebClient", e);
        }
    }
}
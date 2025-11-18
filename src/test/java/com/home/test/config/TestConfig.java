package com.home.test.config;

import com.home.test.config.AppConfig;
import com.home.test.config.FrankfurterApiProperties;
import com.home.test.config.GithubProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
@Import({AppConfig.class, FrankfurterApiProperties.class, GithubProperties.class})
public class TestConfig {

    @Bean
    public FrankfurterApiProperties frankfurterApiProperties() {
        FrankfurterApiProperties properties = new FrankfurterApiProperties();
        properties.setBaseUrl("https://api.frankfurter.app");
        properties.setTimeout(5000);
        return properties;
    }

    @Bean
    public GithubProperties githubProperties() {
        GithubProperties properties = new GithubProperties();
        properties.setUsername("testuser");
        return properties;
    }

    @Bean
    public WebClient testWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.frankfurter.app")
                .build();
    }
}
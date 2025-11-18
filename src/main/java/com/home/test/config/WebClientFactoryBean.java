package com.home.test.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public class WebClientFactoryBean implements FactoryBean<WebClient> {

    private final String baseUrl;
    private final int timeout;

    public WebClientFactoryBean(String baseUrl, int timeout) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
    }

    @Override
    public WebClient getObject() throws Exception {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return WebClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
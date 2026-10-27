package com.riobranco.x.reserva_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient vagaRestClient(@Value("${vaga-api.url}") String baseUrl) {
        // Sem timeout, uma Vaga API travada (ou "dormindo" no plano gratuito do Azure)
        // deixaria a requisição da Reserva pendurada indefinidamente.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
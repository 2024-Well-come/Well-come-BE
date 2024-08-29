package com.wellcome.WellcomeBE.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GoogleReviewApiWebClientConfig {

    @Bean
    public WebClient googlePlaceInfoWebClient(){
        return WebClient.builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/")
                .build();

    }


}

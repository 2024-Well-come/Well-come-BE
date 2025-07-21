package com.wellcome.WellcomeBE.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class GoogleReviewApiWebClientConfig {

    @Value("${google_api.key}")
    private String apiKey;

    private static final String GOOGLE_PLACE_BASE_URL = "https://maps.googleapis.com/maps/api/place/";

    /**
     * ✅ WebClient Bean with timeout + TLS + connection handling for Google Place API
     */
    @Bean(name = "googlePlaceApiWebClient")
    public WebClient googlePlaceApiWebClient() {
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)))
                .secure(); // TLS/SSL 적용

        HttpClient httpClient = HttpClient.from(tcpClient)
                .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(GOOGLE_PLACE_BASE_URL)
                .defaultHeader("accept", "application/json")
                .build();
    }

    /**
     * ✅ (Optional) Separate WebClient bean for flexibility
     */
    @Bean(name = "googlePlaceInfoApiWebClient")
    public WebClient googlePlaceInfoWebClient() {
        return WebClient.builder()
                .baseUrl(GOOGLE_PLACE_BASE_URL)
                .build();
    }

    /**
     * ✅ URI Builder for constructing dynamic Google Place API calls
     */
    public String buildPlaceUri(String endpoint, Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(GOOGLE_PLACE_BASE_URL + endpoint)
                .queryParam("key", apiKey)
                .queryParam("language", "ko");

        additionalParams.forEach(uriBuilder::queryParam);

        String uri = uriBuilder.encode().toUriString();
        log.debug("[GoogleMap URI] {}", uri);
        return uri;
    }

    public String getApiKey() {
        return apiKey;
    }
}

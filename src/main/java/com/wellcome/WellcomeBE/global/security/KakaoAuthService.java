package com.wellcome.WellcomeBE.global.security;

import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoTokenResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.KAKAO_LOGIN_CLIENT_ERROR;


/**
 * 카카오 로그인 관련 API 호출
 */
@Service
@Slf4j
public class KakaoAuthService {

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    private final WebClient webClient;

    public KakaoAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // 카카오 API 호출 - 인가 코드 발급
    public String getAuthorizationCode(){
        return UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .build()
                .toUriString();
    }

    // 카카오 API 호출 - 토큰 받기
    public Mono<KakaoTokenResponse> getAccessToken(String code) {
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                //.queryParam("client_secret", "{CLIENT_SECRET}")
                .build()
                .toUriString();

        return webClient.post()
                .uri(requestUrl)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class);
    }

    // 카카오 API 호출 - 사용자 정보 가져오기
    public Mono<KakaoUserInfoResponse> getUserInfo(String accessToken) {
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com/v2/user/me")
                .queryParam("property_keys", "[\"kakao_account.profile\"]")
                .build()
                .toUriString();

        return webClient.get()
                .uri(requestUrl)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) { // 401 Unauthorized
                        return Mono.error(new CustomException(KAKAO_LOGIN_CLIENT_ERROR, "유효하지 않거나 만료된 토큰입니다."));
                    } else {
                        return Mono.error(new CustomException(KAKAO_LOGIN_CLIENT_ERROR));
                    }
                })
                .bodyToMono(KakaoUserInfoResponse.class);
    }

    // 카카오 API 호출 - 토큰 갱신하기
    public Mono<KakaoTokenResponse> getNewToken(String refreshToken) {
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/token")
                .queryParam("grant_type", "refresh_token")
                .queryParam("client_id", clientId)
                .queryParam("refresh_token", refreshToken)
                .build()
                .toUriString();

        return webClient.post()
                .uri(requestUrl)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class);
    }

    // 카카오 API 호출 - 로그아웃
    public void logout(String accessToken) {
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com/v1/user/logout")
                .build()
                .toUriString();

        webClient.post()
                .uri(requestUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

}

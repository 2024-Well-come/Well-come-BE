package com.wellcome.WellcomeBE.domain.member.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.MemberRepository;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoTokenResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.LoginResponse;
import com.wellcome.WellcomeBE.global.type.SocialLogin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@Slf4j
public class KakaoAuthService {

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    private final WebClient webClient;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public KakaoAuthService(WebClient.Builder webClientBuilder,
                            MemberRepository memberRepository,
                            RedisTemplate<String, Object> redisTemplate) {
        this.webClient = webClientBuilder.build();
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 카카오 로그인 & 회원가입 처리
     */
    public LoginResponse handleKakaoLogin(String code) {

        // 토큰 발급
        KakaoTokenResponse tokenResponse = getAccessToken(code).block();

        // 사용자 정보 가져오기
        KakaoUserInfoResponse userInfoResponse = getUserInfo(tokenResponse.getAccessToken()).block();

        // 회원가입 유무 확인 및 회원가입 진행
        verifyAndRegisterMember(userInfoResponse);

        // refresh token 저장
        saveRefreshToken(userInfoResponse.getId(), tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenExpiresIn());

        // access token, refresh token 응답
        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    // 카카오 API 호출 - 토큰 받기
    private Mono<KakaoTokenResponse> getAccessToken(String code) {
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
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class);
    }

    // 카카오 API 호출 - 사용자 정보 가져오기
    private Mono<KakaoUserInfoResponse> getUserInfo(String accessToken) {
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com/v2/user/me")
                .queryParam("property_keys", "[\"kakao_account.profile\"]")
                .build()
                .toUriString();

        return webClient.post()
                .uri(requestUrl)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class);
    }

    // 회원가입 여부 확인 후 회원가입 진행
    private void verifyAndRegisterMember(KakaoUserInfoResponse userInfoResponse) {
        Optional<Member> member = memberRepository.findByKakaoId(userInfoResponse.getId());
        if(!member.isPresent()){
            memberRepository.save(Member.createKakaoUser(userInfoResponse));
        }
    }

    // refresh token 저장
    private void saveRefreshToken(Long kakaoId, String refreshToken, Integer expiresIn) {
        // key: refreshToken, value: kakaoId
        redisTemplate.opsForValue().set(refreshToken, String.valueOf(kakaoId), Long.valueOf(expiresIn));
    }

    // refresh token 조회
    private String getRefreshToken(String refreshToken) {
        return (String) redisTemplate.opsForValue().get(refreshToken);
    }

    /**
     * 토큰 갱신
     */
    public LoginResponse renewKakaoToken(HttpServletRequest httpServletRequest) {

        // 헤더에서 refresh token 추출
        String refreshToken = extractToken(httpServletRequest);
        if(refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("refresh token이 누락되었습니다.");
            // TODO Custom Exception 처리
        }

        // refresh token 유효성 확인
        if(getRefreshToken(refreshToken) == null) { //refresh token 만료
            // TODO Custom Exception 처리: 재로그인 요청
        }

        // 토큰 재발급 요청
        KakaoTokenResponse newTokenResponse = getNewToken(refreshToken).block();
        Long kakaoId = getUserInfo(newTokenResponse.getAccessToken()).block().getId();

        // refresh token도 갱신된 경우 업데이트
        if(newTokenResponse.getRefreshToken() != null){
            updateRefreshToken(kakaoId, refreshToken, newTokenResponse);
            refreshToken = newTokenResponse.getRefreshToken();
        }

        // 갱신된 access token, refresh token 응답
        return new LoginResponse(newTokenResponse.getAccessToken(), refreshToken);
    }

    // request header에서 token 추출
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 카카오 API 호출 - 토큰 갱신하기
    private Mono<KakaoTokenResponse> getNewToken(String refreshToken) {
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/token")
                .queryParam("grant_type", "refresh_token")
                .queryParam("client_id", clientId)
                .queryParam("refresh_token", refreshToken)
                .build()
                .toUriString();

        return webClient.post()
                .uri(requestUrl)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class);
    }

    // refresh token이 갱신된 경우 업데이트
    private String updateRefreshToken(Long kakaoId, String oldRefreshToken, KakaoTokenResponse newTokenResponse){
        String newRefreshToken = newTokenResponse.getRefreshToken();

        redisTemplate.delete(oldRefreshToken);
        saveRefreshToken(kakaoId, newRefreshToken, newTokenResponse.getRefreshTokenExpiresIn());

        return newRefreshToken;
    }

    /**
     * 카카오 로그아웃
     */
    public void handleKakaoLogout(HttpServletRequest httpServletRequest) {

        // 헤더에서 access token 추출
        String accessToken = extractToken(httpServletRequest);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("acess token이 누락되었습니다.");
            // TODO Custom Exception 처리
        }

        // 로그아웃 처리
        logout(accessToken);

        // TODO refresh token 삭제

    }

    // 카카오 API 호출 - 로그아웃
    private void logout(String accessToken) {
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

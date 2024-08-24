package com.wellcome.WellcomeBE.domain.member.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.MemberRepository;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoTokenResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.LoginResponse;
import com.wellcome.WellcomeBE.global.type.SocialLogin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
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

    // 토큰 받기
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

    // 사용자 정보 가져오기
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
            Member.createKakaoUser(userInfoResponse);
        }
    }

    // refresh token 저장
    private void saveRefreshToken(Long kakaoId, String refreshToken, Integer expiresIn) {
        redisTemplate.opsForValue().set(String.valueOf(kakaoId), refreshToken, Long.valueOf(expiresIn));
    }

    // refresh token 조회
    private String getRefreshToken(Long kakaoId) {
        return (String) redisTemplate.opsForValue().get(String.valueOf(kakaoId));
    }

}

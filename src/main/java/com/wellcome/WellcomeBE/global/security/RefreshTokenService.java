package com.wellcome.WellcomeBE.global.security;

import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis를 활용한 refresh token 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Qualifier("strRedisTemplate")
public class RefreshTokenService {

    private final RedisTemplate<String, String> strRedisTemplate;

    // refresh token 저장
    public void saveRefreshToken(Long kakaoId, String refreshToken, Integer expiresIn) {
        // key: refreshToken, value: kakaoId
        strRedisTemplate.opsForValue().set(refreshToken, String.valueOf(kakaoId), Long.valueOf(expiresIn), TimeUnit.SECONDS);
    }

    // kakaoId 조회
    public String getKakaoIdByRefreshToken(String refreshToken) {
        return strRedisTemplate.opsForValue().get(refreshToken);
    }

    // refresh token 삭제
    public void deleteRefreshToken(String refreshToken) {
        strRedisTemplate.delete(refreshToken);
    }

    // refresh token이 갱신된 경우 업데이트
    public String updateRefreshToken(Long kakaoId, String oldRefreshToken, KakaoTokenResponse newTokenResponse){
        String newRefreshToken = newTokenResponse.getRefreshToken();

        deleteRefreshToken(oldRefreshToken);
        saveRefreshToken(kakaoId, newRefreshToken, newTokenResponse.getRefreshTokenExpiresIn());

        return newRefreshToken;
    }

}

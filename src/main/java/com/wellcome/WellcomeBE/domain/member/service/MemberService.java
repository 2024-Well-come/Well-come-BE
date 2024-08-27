package com.wellcome.WellcomeBE.domain.member.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.MemberRepository;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoTokenResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.LoginResponse;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.KakaoAuthService;
import com.wellcome.WellcomeBE.global.security.RefreshTokenService;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.REFRESH_TOKEN_EXPIRED;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOKEN_MISSING;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final TokenProvider tokenProvider;
    private final KakaoAuthService kakaoAuthService;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;

    /**
     * 카카오 로그인 & 회원가입 처리
     */
    public LoginResponse handleKakaoLogin(String code) {

        // 토큰 발급
        KakaoTokenResponse tokenResponse = kakaoAuthService.getAccessToken(code).block();

        // 사용자 정보 가져오기
        KakaoUserInfoResponse userInfoResponse = kakaoAuthService.getUserInfo(tokenResponse.getAccessToken()).block();

        // 회원가입 유무 확인 및 회원가입 진행
        verifyAndRegisterMember(userInfoResponse);

        // refresh token 저장
        refreshTokenService.saveRefreshToken(userInfoResponse.getId(), tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenExpiresIn());

        // access token, refresh token 응답
        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    // 회원가입 여부 확인 후 회원가입 진행
    private void verifyAndRegisterMember(KakaoUserInfoResponse userInfoResponse) {
        Optional<Member> member = memberRepository.findByKakaoId(userInfoResponse.getId());
        if(!member.isPresent()){
            memberRepository.save(Member.createKakaoUser(userInfoResponse));
        }
    }


    /**
     * 토큰 갱신
     */
    public LoginResponse renewKakaoToken(HttpServletRequest httpServletRequest) {

        // refresh token 추출
        String refreshToken = tokenProvider.extractToken(httpServletRequest);
        if(refreshToken == null) {
            throw new CustomException(TOKEN_MISSING, "refresh token이 누락되었습니다.");
        }

        // refresh token 유효성 확인
        String savedKakaoId = refreshTokenService.getKakaoIdByRefreshToken(refreshToken);
        if(savedKakaoId == null) { //refresh token 만료
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }

        // 토큰 재발급 요청
        KakaoTokenResponse newTokenResponse = kakaoAuthService.getNewToken(refreshToken).block();

        // refresh token도 갱신된 경우 업데이트
        if(newTokenResponse.getRefreshToken() != null){
            Long kakaoId = kakaoAuthService.getUserInfo(newTokenResponse.getAccessToken()).block().getId();

            refreshTokenService.updateRefreshToken(kakaoId, refreshToken, newTokenResponse);
            refreshToken = newTokenResponse.getRefreshToken();
        }

        // 갱신된 access token, refresh token 응답
        return new LoginResponse(newTokenResponse.getAccessToken(), refreshToken);
    }


    /**
     * 카카오 로그아웃
     */
    public void handleKakaoLogout(HttpServletRequest httpServletRequest, String refreshToken) {

        // access token 추출
        String accessToken = tokenProvider.extractToken(httpServletRequest);
        if (accessToken == null) {
            throw new CustomException(TOKEN_MISSING, "acess token이 누락되었습니다.");
        }

        // 로그아웃 처리
        kakaoAuthService.logout(accessToken);

        // refresh token 삭제
        // TODO -> 우선은 refresh token도 받는 걸로 생각하고 구현, 수정 필요
        refreshTokenService.deleteRefreshToken(refreshToken);

        // SecurityContextHolder 초기화
        tokenProvider.clearContext();
    }

}

package com.wellcome.WellcomeBE.domain.member.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoTokenResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.LoginResponse;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.global.image.S3Service;
import com.wellcome.WellcomeBE.global.security.KakaoAuthService;
import com.wellcome.WellcomeBE.global.security.RefreshTokenService;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private KakaoAuthService kakaoAuthService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Service s3Service;

    /**
     * 회원가입
     */
    @Test
    void handleKakaoSignup_가입되지_않은_유저는_회원가입_진행() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String accessToken = "test-access-token";
        when(tokenProvider.extractToken(request)).thenReturn(accessToken);

        // - 카카오 사용자 정보
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        KakaoUserInfoResponse.KakaoAccount account = mock(KakaoUserInfoResponse.KakaoAccount.class);
        KakaoUserInfoResponse.KakaoAccount.Profile profile = mock(KakaoUserInfoResponse.KakaoAccount.Profile.class);

        when(kakaoAuthService.getUserInfo(accessToken)).thenReturn(Mono.just(userInfo));
        when(userInfo.getId()).thenReturn(12345L);
        when(userInfo.getKakaoAccount()).thenReturn(account);
        when(account.getProfile()).thenReturn(profile);
        when(profile.getIsDefaultImage()).thenReturn(false);
        when(profile.getProfileImgUrl()).thenReturn("test-profile-img-url");

        // - 해당 카카오 계정으로 회원가입 되지 않은 상태로 설정
        when(memberRepository.findByKakaoId(userInfo.getId())).thenReturn(Optional.empty());

        // when
        memberService.handleKakaoSignup(request);

        // then
        verify(memberRepository, times(1)).save(any());
    }

    @Test
    void handleKakaoSignup_이미_가입된_유저는_회원가입_진행하지_않음() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String accessToken = "test-access-token";
        when(tokenProvider.extractToken(request)).thenReturn(accessToken);

        // - 카카오 사용자 정보
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        when(kakaoAuthService.getUserInfo(accessToken)).thenReturn(Mono.just(userInfo));
        when(userInfo.getId()).thenReturn(12345L);

        // - 해당 카카오 계정으로 회원가입된 상태로 설정
        Member member = mock(Member.class);
        when(memberRepository.findByKakaoId(userInfo.getId())).thenReturn(Optional.of(member));

        // when
        memberService.handleKakaoSignup(request);

        // then
        verify(memberRepository, never()).save(any());
    }

    /**
     * 카카오 로그인
     */
    @Test
    void handleKakaoLogin_로그인(){
        // given
        String code = "test-code";
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";
        long kakaoId = 12345;

        MemberService spyService = spy(memberService);

        // - 토큰 발급
        KakaoTokenResponse tokens = mock(KakaoTokenResponse.class);
        when(kakaoAuthService.getAccessToken(code)).thenReturn(Mono.just(tokens));
        when(tokens.getAccessToken()).thenReturn(accessToken);
        when(tokens.getRefreshToken()).thenReturn(refreshToken);

        // - 카카오 사용자 정보
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        when(userInfo.getId()).thenReturn(kakaoId);
        when(kakaoAuthService.getUserInfo(tokens.getAccessToken())).thenReturn(Mono.just(userInfo));

        // - 유저가 회원가입된 상태로 설정
        Member member = mock(Member.class);
        when(memberRepository.findByKakaoId(userInfo.getId())).thenReturn(Optional.of(member));

        // when
        LoginResponse response = spyService.handleKakaoLogin(code);

        // then
        // - 회원가입 진행 x
        verify(spyService, times(1)).verifyAndRegisterMember(userInfo);
        verify(memberRepository, never()).save(any());

        // - refresh token 저장 검증
        verify(refreshTokenService, times(1)).saveRefreshToken(any(), any(), any());

        // - 토큰 검증
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void handleKakaoLogin_로그인_후_가입되지_않은_유저는_회원가입_진행(){
        // given
        String code = "test-code";
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";
        long kakaoId = 12345;

        MemberService spyService = spy(memberService);
        doReturn(mock(Member.class)).when(spyService).createKakaoUser(any());

        // - 토큰 발급
        KakaoTokenResponse tokens = mock(KakaoTokenResponse.class);
        when(kakaoAuthService.getAccessToken(code)).thenReturn(Mono.just(tokens));
        when(tokens.getAccessToken()).thenReturn(accessToken);
        when(tokens.getRefreshToken()).thenReturn(refreshToken);

        // - 카카오 사용자 정보
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        when(userInfo.getId()).thenReturn(kakaoId);
        when(kakaoAuthService.getUserInfo(tokens.getAccessToken())).thenReturn(Mono.just(userInfo));

        // - 유저가 회원가입되지 않은 상태로 설정
        when(memberRepository.findByKakaoId(userInfo.getId())).thenReturn(Optional.empty());

        // when
        LoginResponse response = spyService.handleKakaoLogin(code);

        // then
        // - 회원가입 진행 o
        verify(spyService, times(1)).verifyAndRegisterMember(userInfo);
        verify(memberRepository, times(1)).save(any());

        // - refresh token 저장 검증
        verify(refreshTokenService, times(1)).saveRefreshToken(any(), any(), any());

        // - 토큰 검증
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    /**
     * 토큰 갱신
     */
    @Test
    void renewKakaoToken_accessToken은_갱신되고_refreshToken은_갱신되지_않음(){
        // given
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        String newAccessToken = "test-updated-access-token";
        String refreshToken = "test-refresh-token";
        long kakaoId = 12345;

        // - refresh token 추출
        when(tokenProvider.extractToken(httpServletRequest)).thenReturn(refreshToken);

        // - refresh token이 유효한 경우 kakaoId를 반환
        when(refreshTokenService.getKakaoIdByRefreshToken(refreshToken)).thenReturn(String.valueOf(kakaoId));

        // - access token 재발급 / refresh token 갱신 x
        KakaoTokenResponse tokens = mock(KakaoTokenResponse.class);
        when(tokens.getAccessToken()).thenReturn(newAccessToken);

        when(kakaoAuthService.getNewToken(refreshToken)).thenReturn(Mono.just(tokens));

        // when
        LoginResponse response = memberService.renewKakaoToken(httpServletRequest);

        // then
        // - access token 갱신 o
        assertEquals(newAccessToken, response.getAccessToken());
        // - refresh token 갱신 x
        assertEquals(refreshToken, response.getRefreshToken());
        verify(refreshTokenService, never()).updateRefreshToken(any(), any(), any());
    }

    @Test
    void renewKakaoToken_accessToken과_refreshToken이_모두_갱신됨(){
        // given
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        String newAccessToken = "test-updated-access-token";
        String refreshToken = "test-refresh-token";
        String newRefreshToken = "test-updated-refresh-token";
        long kakaoId = 12345;

        // - refresh token 추출
        when(tokenProvider.extractToken(httpServletRequest)).thenReturn(refreshToken);

        // - refresh token이 유효한 경우 kakaoId를 반환
        when(refreshTokenService.getKakaoIdByRefreshToken(refreshToken)).thenReturn(String.valueOf(kakaoId));

        // - access token 재발급
        KakaoTokenResponse tokens = mock(KakaoTokenResponse.class);
        when(tokens.getAccessToken()).thenReturn(newAccessToken);
        when(kakaoAuthService.getNewToken(refreshToken)).thenReturn(Mono.just(tokens));

        // - refresh token 갱신
        when(tokens.getRefreshToken()).thenReturn(newRefreshToken);

        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        when(kakaoAuthService.getUserInfo(newAccessToken)).thenReturn(Mono.just(userInfo));
        when(userInfo.getId()).thenReturn(kakaoId);

        // when
        LoginResponse response = memberService.renewKakaoToken(httpServletRequest);

        // then
        // - access token 갱신 o
        assertEquals(newAccessToken, response.getAccessToken());
        // - refresh token 갱신 o
        assertEquals(newRefreshToken, response.getRefreshToken());
        verify(refreshTokenService, times(1)).updateRefreshToken(any(), any(), any());
    }

    /**
     * 로그아웃
     */
    @Test
    void handleKakaoLogout_로그아웃(){
        // given
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";

        // - access token 추출
        when(tokenProvider.extractToken(httpServletRequest)).thenReturn(accessToken);

        // when
        memberService.handleKakaoLogout(httpServletRequest, refreshToken);

        // then
        verify(kakaoAuthService, times(1)).logout(accessToken);
        verify(refreshTokenService, times(1)).deleteRefreshToken(refreshToken);
        verify(tokenProvider, times(1)).clearContext();
    }

}
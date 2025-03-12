package com.wellcome.WellcomeBE.domain.member.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
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

}
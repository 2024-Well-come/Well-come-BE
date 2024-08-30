package com.wellcome.WellcomeBE.global.security;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.AUTHENTICATION_NOT_FOUND;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.MEMBER_NOT_FOUND;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {
    private final KakaoAuthService kakaoAuthService;
    private final CustomUserDetailsService customUserDetailsService;
    private final MemberRepository memberRepository;

    // request header에서 token 추출
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰의 사용자 정보를 기반으로 Authentication 객체 생성
    public Authentication getAuthentication(String accessToken) {

        Long kakaoId = getKakaoIdByToken(accessToken);

        // 사용자 정보를 기반으로 CustomUserDetails 생성
        CustomUserDetails customUserDetails =
                (CustomUserDetails) customUserDetailsService.loadUserByUsername(String.valueOf(kakaoId));

        // Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    // * 로그인이 필수인 엔드포인트에서 사용
    // SecurityContextHolder 를 통해 memberId를 가져옴
    public Long getMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getMemberId();
        }
        throw new CustomException(AUTHENTICATION_NOT_FOUND);
    }

    // * 로그인이 필수가 아닌 엔드포인트에서 사용
    // SecurityContextHolder 또는 HttpServletRequest 를 통해 memberId를 가져옴
    public Long getMemberIdByServlet(HttpServletRequest request) {

        // 먼저 SecurityContextHolder 에서 memberId를 확인
        Long memberId = getMemberId();
        if (memberId != null) {
            return memberId;
        }

        // SecurityContextHolder 에 정보가 없을 경우, HttpServletRequest 에서 토큰 확인
        String token = extractToken(request);
        if (token != null) {
            Long kakaoId = getKakaoIdByToken(token);
            Member member = memberRepository.findByKakaoId(kakaoId)
                    .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
            return member.getId();
        }

        return null;
    }

    // 토큰을 통해 카카오 사용자 정보 조회 API 호출, kakaoId 조회
    private Long getKakaoIdByToken(String accessToken) {
        KakaoUserInfoResponse userInfoResponse = kakaoAuthService.getUserInfo(accessToken).block();
        return userInfoResponse.getId();
    }

    // SecurityContextHolder 초기화
    public void clearContext(){
        SecurityContextHolder.clearContext();
    }

}

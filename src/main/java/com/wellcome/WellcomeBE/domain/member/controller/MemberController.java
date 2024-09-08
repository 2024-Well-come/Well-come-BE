package com.wellcome.WellcomeBE.domain.member.controller;

import com.wellcome.WellcomeBE.domain.member.dto.request.LogoutRequest;
import com.wellcome.WellcomeBE.domain.member.dto.response.LoginResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.MemberProfileResponse;
import com.wellcome.WellcomeBE.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    // 카카오 로그인 요청
    @GetMapping("/api/oauth2/kakao/login")
    public void login(HttpServletResponse response) throws IOException {
        memberService.login(response);
    }

    // 토큰 발급
    @GetMapping("/api/oauth2/kakao")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestParam("code") String code){
        return ResponseEntity.ok(memberService.handleKakaoLogin(code));
    }

    // 토큰 갱신
    @PostMapping("/api/oauth2/kakao/refresh")
    public ResponseEntity<LoginResponse> kakaoRenewToken(HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(memberService.renewKakaoToken(httpServletRequest));
    }

    // 카카오 로그아웃
    @PostMapping("/api/oauth2/kakao/logout")
    public ResponseEntity<Void> kakaoLogout(HttpServletRequest httpServletRequest,
                                            @RequestBody LogoutRequest logoutRequest){
        memberService.handleKakaoLogout(httpServletRequest, logoutRequest.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    // 사용자 프로필 조회
    @GetMapping("/api/mypage/profile")
    public ResponseEntity<MemberProfileResponse> getMemberProfile(){
        return ResponseEntity.ok(memberService.getMemberProfile());
    }

}

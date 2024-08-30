package com.wellcome.WellcomeBE.domain.member.controller;

import com.wellcome.WellcomeBE.domain.member.dto.request.LogoutRequest;
import com.wellcome.WellcomeBE.domain.member.dto.response.LoginResponse;
import com.wellcome.WellcomeBE.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/oauth2/kakao")
public class MemberController {

    private final MemberService memberService;

    // 카카오 로그인
    @GetMapping
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestParam("code") String code){
        return ResponseEntity.ok(memberService.handleKakaoLogin(code));
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> kakaoRenewToken(HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(memberService.renewKakaoToken(httpServletRequest));
    }

    // 카카오 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> kakaoLogout(HttpServletRequest httpServletRequest,
                                            @RequestBody LogoutRequest logoutRequest){
        memberService.handleKakaoLogout(httpServletRequest, logoutRequest.getRefreshToken());
        return ResponseEntity.ok().build();
    }

}

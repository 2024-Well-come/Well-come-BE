package com.wellcome.WellcomeBE.domain.member;

import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoTokenResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.KakaoUserInfoResponse;
import com.wellcome.WellcomeBE.domain.member.dto.response.LoginResponse;
import com.wellcome.WellcomeBE.domain.member.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final KakaoAuthService kakaoService;

    // 카카오 로그인
    @GetMapping("/api/oauth2/kakao")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestParam("code") String code){
        return ResponseEntity.ok(kakaoService.handleKakaoLogin(code));
    }

}

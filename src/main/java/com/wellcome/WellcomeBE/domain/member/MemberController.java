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

        // 토큰 발급
        KakaoTokenResponse tokenResponse = kakaoService.getAccessToken(code).block();

        // 사용자 정보 가져오기
        KakaoUserInfoResponse userInfoResponse = kakaoService.getUserInfo(tokenResponse.getAccessToken()).block();

        // 회원가입 유무 확인 및 회원가입 진행
        kakaoService.verifyAndRegisterMember(userInfoResponse);

        // refresh token 저장
        kakaoService.saveRefreshToken(tokenResponse, userInfoResponse);

        // 카카오에서 발급받은 access token, refresh token 응답
        LoginResponse result = new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

        return ResponseEntity.ok(result);
    }


}

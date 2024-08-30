package com.wellcome.WellcomeBE.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 카카오 로그인 - 사용자 정보 가져오기
 */
@Data
public class KakaoUserInfoResponse {

    @JsonProperty("id")
    private Long id; //회원번호

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Data
    public static class KakaoAccount {

        @JsonProperty("profile_needs_agreement")
        private Boolean profileNeedsAgreement;

        @JsonProperty("profile_nickname_needs_agreement")
        private Boolean profileNicknameNeedsAgreement;

        @JsonProperty("profile_image_needs_agreement")
        private Boolean profileImageNeedsAgreement;

        @JsonProperty("profile")
        private Profile profile;

        @Data
        public static class Profile {

            @JsonProperty("nickname")
            private String nickname;

            @JsonProperty("profile_image_url")
            private String profileImgUrl;

            @JsonProperty("is_default_image")
            private Boolean isDefaultImage;
        }
    }
}

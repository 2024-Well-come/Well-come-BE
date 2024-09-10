package com.wellcome.WellcomeBE.domain.member;


import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.global.type.Role;
import com.wellcome.WellcomeBE.global.type.SocialLogin;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Lob
    @Column(name = "profile_img")
    private String profileImg;

    private String nickname;

    @Enumerated(value = STRING)
    private Role role;

    @Enumerated(value = STRING)
    private SocialLogin socialType;

    @Column(nullable = false) //현재 카카오 로그인만 가능
    private Long kakaoId;
    

    @Builder
    private Member(String nickname, String profileImg, Role role,
                   SocialLogin socialType, Long kakaoId) {
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.role = role;
        this.socialType = socialType;
        this.kakaoId = kakaoId;
    }
}

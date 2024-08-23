package com.wellcome.WellcomeBE.domain.member;


import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.global.type.SocialLogin;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Getter
@Builder
@AllArgsConstructor
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
    private SocialLogin socialType;

    @Column(nullable = false) //현재 카카오 로그인만 가능
    private Long kakaoId;

}

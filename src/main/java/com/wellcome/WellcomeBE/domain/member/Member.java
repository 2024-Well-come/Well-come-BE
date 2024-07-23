package com.wellcome.WellcomeBE.domain.member;


import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "profile_img", nullable = false)
    private String profileImg;

    private String nickname;

}

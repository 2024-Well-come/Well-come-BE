package com.wellcome.WellcomeBE.domain.like;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
//@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
public class Liked extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liked_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wellness_info_id", nullable = false)
    private WellnessInfo wellnessInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    private Liked(WellnessInfo wellnessInfo, Member member){
        this.wellnessInfo = wellnessInfo;
        this.member = member;
    }

    public static Liked create(WellnessInfo wellnessInfo, Member member){
        return Liked.builder()
                .wellnessInfo(wellnessInfo)
                .member(member)
                .build();
    }
}

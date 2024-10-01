package com.wellcome.WellcomeBE.domain.support;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Support {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "support_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wellness_info_id")
    private WellnessInfo wellnessInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportType supportType;

    public enum SupportType {
        COMMUNITY, TRIP_PLAN_PLACE
    }


    public static Support createcommunitySupport(Community community, Member member){
        return Support.builder()
                .member(member)
                .community(community)
                .supportType(SupportType.COMMUNITY)
                .build();
    }

    public static Support createWellnessInfoSupport(Community community,WellnessInfo wellnessInfo, Member member){
        return Support.builder()
                .member(member)
                .wellnessInfo(wellnessInfo)
                .community(community)
                .supportType(SupportType.TRIP_PLAN_PLACE)
                .build();
    }

}

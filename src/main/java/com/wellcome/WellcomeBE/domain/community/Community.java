package com.wellcome.WellcomeBE.domain.community;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
//@Builder
//@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Community extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    //@Builder.Default
    @Column(nullable = false)
    private Long view = 0L;

    //@Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType = PostType.TRIP_PLAN;

    public enum PostType{
        GENERAL, TRIP_PLAN
    }

    @Builder
    private Community(Member member, TripPlan tripPlan,
                      String title, String content
    ){
        this.member = member;
        this.tripPlan= tripPlan;
        this.title = title;
        this.content = content;
    }

    public static Community createByTripPlan(Member member, TripPlan tripPlan,
                                             String title, String content){
        return Community.builder()
                .member(member)
                .tripPlan(tripPlan)
                .title(title)
                .content(content)
                .build();
    }

}

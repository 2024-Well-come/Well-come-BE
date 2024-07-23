package com.wellcome.WellcomeBE.domain.communityImg;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityImg extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id",nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_place_id")
    private TripPlanPlace tripPlanPlace;

    @Lob
    @Column(name = "img_url",nullable = false)
    private String imgUrl;
    
}

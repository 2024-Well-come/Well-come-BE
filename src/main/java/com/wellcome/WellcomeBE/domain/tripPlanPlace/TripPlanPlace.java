package com.wellcome.WellcomeBE.domain.tripPlanPlace;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class TripPlanPlace extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_plan_place_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wellness_info_id", nullable = false)
    private WellnessInfo wellnessInfo;

    private Integer rating;
    private String review;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    protected TripPlan.Status status = TripPlan.Status.ACTIVE; // 여행지 삭제 여부

    public enum Status {
        ACTIVE, INACTIVE;
    }

    public void updatePlaceReview(Integer rating, String review) {
        this.rating = rating;
        this.review = review;
    }

    // 상태 업데이트 메서드 추가
    public void markAsInactive() { this.status = TripPlan.Status.INACTIVE;}

}

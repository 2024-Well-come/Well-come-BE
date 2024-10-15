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
    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'")
    private TripPlan.Status status;

    public enum Status {
        ACTIVE, INACTIVE;
    }

    // 엔티티가 DB에 저장되기 전에 호출되는 메서드
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = TripPlan.Status.ACTIVE; // 기본값 설정
        }
    }

    public void updatePlaceReview(Integer rating, String review) {
        this.rating = rating;
        this.review = review;
    }

    // 상태 업데이트 메서드 추가
    public void markAsInactive() { this.status = TripPlan.Status.INACTIVE;}

}

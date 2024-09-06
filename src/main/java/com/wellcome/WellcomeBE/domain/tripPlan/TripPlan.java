package com.wellcome.WellcomeBE.domain.tripPlan;

import com.wellcome.WellcomeBE.domain.BaseTimeEntity;
import com.wellcome.WellcomeBE.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class TripPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_plan_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    private LocalDate startDate;
    private LocalDate endDate;

    private String summary;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void updateEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

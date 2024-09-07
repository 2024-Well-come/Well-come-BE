package com.wellcome.WellcomeBE.domain.tripPlan.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan,Long> {
    List<TripPlan> findByMember(Member member);

    @Query("SELECT tp FROM TripPlan tp " +
            "JOIN FETCH tp.member " +
            "WHERE tp.id IN :idList")
    List<TripPlan> findByIdIn(@Param("idList") List<Long> tripPlanIdList);

    Optional<TripPlan> findByIdAndMemberId(Long tripPlanId, Long memberId);
    @Query("SELECT t FROM TripPlan t " +
            "LEFT JOIN FETCH t.tripPlanPlaces tp " +
            "LEFT JOIN FETCH tp.wellnessInfo wi " +
            "ORDER BY " +
            "CASE WHEN t.startDate >= CURRENT_DATE THEN 0 ELSE 1 END, " +
            "CASE WHEN t.startDate IS NULL THEN t.createdAt END DESC, " +
            "CASE WHEN t.startDate IS NOT NULL THEN t.startDate END DESC, " +
            "t.createdAt DESC")
    Page<TripPlan> findUpcomingPlans(PageRequest pageRequest);


    @Query("SELECT t FROM TripPlan t " +
            "LEFT JOIN FETCH t.tripPlanPlaces tp " +
            "LEFT JOIN FETCH tp.wellnessInfo wi " +
            "ORDER BY t.createdAt DESC")
    Page<TripPlan> findCreateLatestPlans(PageRequest pageRequest);

    @Query("SELECT t FROM TripPlan t LEFT JOIN FETCH t.tripPlanPlaces tp LEFT JOIN FETCH tp.wellnessInfo wi WHERE t.startDate >= CURRENT_DATE ORDER BY t.startDate ASC")
    List<TripPlan> findAllByTripStartDateAfter();

}

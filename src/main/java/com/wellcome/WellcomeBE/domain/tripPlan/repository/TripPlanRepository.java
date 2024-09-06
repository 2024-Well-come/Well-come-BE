package com.wellcome.WellcomeBE.domain.tripPlan.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan,Long> {
    List<TripPlan> findByMember(Member member);

    @Query("SELECT tp FROM TripPlan tp " +
            "JOIN FETCH tp.member " +
            "WHERE tp.id IN :idList")
    List<TripPlan> findByIdIn(@Param("idList") List<Long> tripPlanIdList);

    Optional<TripPlan> findByIdAndMemberId(Long tripPlanId, Long memberId);

}

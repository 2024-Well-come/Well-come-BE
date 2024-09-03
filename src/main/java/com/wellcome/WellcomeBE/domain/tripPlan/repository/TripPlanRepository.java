package com.wellcome.WellcomeBE.domain.tripPlan.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPlanRepository extends JpaRepository<TripPlan,Long> {
    List<TripPlan> findByMember(Member member);
}

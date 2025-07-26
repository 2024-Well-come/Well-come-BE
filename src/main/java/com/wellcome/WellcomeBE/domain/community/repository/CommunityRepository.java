package com.wellcome.WellcomeBE.domain.community.repository;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long>, CommunityRepositoryCustom {

    Optional<Community> findByTripPlan(TripPlan tripPlan);
    @Query("SELECT c.tripPlan.id FROM Community c WHERE c.tripPlan.id IN :tripPlanIdList")
    List<Long> findReferencedTripPlanIds(@Param("tripPlanIdList") List<Long> tripPlanIdList);


    Page<Community> findByPostTypeOrderByCreatedAtDesc(Pageable pageable, Community.PostType tripPlan);

    List<Community> findByTripPlanInOrderByCreatedAtDesc(Pageable pageable, List<TripPlan> tripPlanList);

    Optional<Community> findById(Long communityId);

}

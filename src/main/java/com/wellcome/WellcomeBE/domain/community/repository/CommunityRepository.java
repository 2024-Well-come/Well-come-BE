package com.wellcome.WellcomeBE.domain.community.repository;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByTripPlan(TripPlan tripPlan);

    Page<Community> findByPostTypeOrderByCreatedAtDesc(Pageable pageable, Community.PostType tripPlan);

    @Query("SELECT c FROM Community c " +
            "LEFT JOIN Support s ON c.id = s.community.id " +
            "WHERE c.postType = :postType " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(s.id) DESC, c.createdAt DESC")
    Page<Community> findByPostTypeOrderBySupportCount(Pageable pageable, @Param("postType") Community.PostType type);

    List<Community> findByTripPlanInOrderByCreatedAtDesc(Pageable pageable, List<TripPlan> tripPlanList);

}

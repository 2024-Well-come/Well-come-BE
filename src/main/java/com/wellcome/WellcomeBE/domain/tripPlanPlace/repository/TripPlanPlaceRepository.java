package com.wellcome.WellcomeBE.domain.tripPlanPlace.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripPlanPlaceRepository extends JpaRepository<TripPlanPlace,Long> {

    @Query("SELECT tpp FROM TripPlanPlace tpp " +
            "JOIN FETCH tpp.tripPlan " +
            "WHERE tpp.tripPlan.id IN :tripPlanIdList")
    List<TripPlanPlace> findByTripPlanIdIn(@Param("tripPlanIdList") List<Long> tripPlanIdList);


    @Query("SELECT tpp FROM TripPlanPlace tpp " +
            "JOIN FETCH tpp.tripPlan " +
            "WHERE tpp.id IN :idList")
    List<TripPlanPlace> findByIdIn(@Param("idList") List<Long> tripPlanPlaceIdList);

    @Query("SELECT tpp FROM TripPlanPlace tpp " +
            "JOIN FETCH tpp.wellnessInfo w " +
            "WHERE tpp.tripPlan.id = :planId " +
            "AND (:thema IS NULL OR tpp.wellnessInfo.thema = :thema) " +
            "ORDER BY tpp.createdAt DESC")
    Page<TripPlanPlace> findByTripPlanIdAndThema(PageRequest pageRequest,
                                                 @Param("planId") Long planId,
                                                 @Param("thema") Thema thema);

    @Query("SELECT COUNT(tp) > 0 FROM TripPlanPlace tp " +
            "JOIN tp.wellnessInfo wi " +
            "JOIN tp.tripPlan t " +
            "WHERE t = :tripPlan " +
            "AND wi = :wellnessInfo " +
            "AND t.member = :member")
    boolean existsByTripPlanAndWellnessInfoAndMember(@Param("tripPlan") TripPlan tripPlan,
                                                     @Param("wellnessInfo") WellnessInfo wellnessInfo,
                                                     @Param("member") Member member);

    Optional<TripPlanPlace> findByTripPlanIdAndWellnessInfoId(Long planId, Long wellnessInfoId);

    @Query("SELECT tpp.tripPlan FROM TripPlanPlace tpp " +
            "WHERE tpp.wellnessInfo = :wellnessInfo " +
            "AND (tpp.rating IS NOT NULL OR tpp.review IS NOT NULL)")
    List<TripPlan> findByWellnessInfoAndReviewConditionsExist(WellnessInfo wellnessInfo);
}

package com.wellcome.WellcomeBE.domain.tripPlanPlace.repository;

import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

}

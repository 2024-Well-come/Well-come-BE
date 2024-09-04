package com.wellcome.WellcomeBE.domain.tripPlanPlace.repository;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripPlanPlaceRepository extends JpaRepository<TripPlanPlace,Long> {

    @Query("SELECT tpp FROM TripPlanPlace tpp " +
            "JOIN FETCH tpp.tripPlan " +
            "WHERE tpp.tripPlan.id IN :tripPlanIdList")
    List<TripPlanPlace> findByIdIn(@Param("tripPlanIdList") List<Long> tripPlanIdList);

}

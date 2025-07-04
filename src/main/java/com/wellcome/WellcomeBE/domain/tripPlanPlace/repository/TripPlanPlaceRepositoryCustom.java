package com.wellcome.WellcomeBE.domain.tripPlanPlace.repository;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripPlanPlaceRepositoryCustom {

    Page<TripPlanPlace> findByTripPlanIdAndThema(PageRequest pageRequest,
                                                 @Param("planId") Long planId,
                                                 @Param("thema") Thema thema);

    List<TripPlan> findByWellnessInfoAndReviewConditionsExist(WellnessInfo wellnessInfo);

}

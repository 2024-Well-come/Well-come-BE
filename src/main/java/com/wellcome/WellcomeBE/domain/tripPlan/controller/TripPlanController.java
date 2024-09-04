package com.wellcome.WellcomeBE.domain.tripPlan.controller;

import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.service.TripPlanService;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceRequest;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.service.TripPlanPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-trips")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;
    private final TripPlanPlaceService tripPlanPlaceService;

    @PostMapping("/plans")
    public ResponseEntity<?> addTripPlan(@RequestBody TripPlanRequest request){
        tripPlanService.createTripPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/plans/{planId}/places")
    public ResponseEntity<?> addTripPlanPlace(@PathVariable Long planId, @RequestBody TripPlanPlaceRequest request){
        tripPlanPlaceService.createTripPlanPlace(planId,request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/plans/brief")
    public ResponseEntity<TripPlanResponse.TripPlanListResponse> tripPlanList(){
        return ResponseEntity.ok(tripPlanService.getTripPlanList());
    }

    @DeleteMapping("/plans")
    public ResponseEntity<Void> deleteTripPlan(@RequestBody TripPlanDeleteRequest request){
        tripPlanService.deleteTripPlan(request);
        return ResponseEntity.ok().build();
    }

}

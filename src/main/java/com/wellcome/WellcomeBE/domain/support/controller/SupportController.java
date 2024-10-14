package com.wellcome.WellcomeBE.domain.support.controller;

import com.wellcome.WellcomeBE.domain.support.dto.SupportRequest;
import com.wellcome.WellcomeBE.domain.support.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {
    private final SupportService supportService;

    @PostMapping("/community/{communityId}")
    public ResponseEntity<Void> createCommunitySupport(@PathVariable Long communityId) {
        supportService.createCommunitySupport(communityId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/community/{communityId}")
    public ResponseEntity<Void> deleteCommunitySupport(@PathVariable Long communityId) {
        supportService.deleteCommunitySupport(communityId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/community/wellness")
    public ResponseEntity<Void> createCommunityInWellnessSupport(@Valid @RequestBody SupportRequest.CreateCommunityInWellnessSupportRequestDto requestDto) {
        supportService.createTripPlanPlaceSupport(requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/community/wellness")
    public ResponseEntity<Void> deleteCommunityInWellnessSupport(@Valid @RequestBody SupportRequest.DeleteCommunityInWellnessSupportRequestDto requestDto) {
        supportService.deleteCommunityInWellnessSupport(requestDto);
        return ResponseEntity.ok().build();
    }

}
package com.wellcome.WellcomeBE.domain.like.controller;

import com.wellcome.WellcomeBE.domain.like.dto.response.LikedResponse;
import com.wellcome.WellcomeBE.domain.like.service.LikedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LikedController {

    private final LikedService likedService;

    // 좋아요 등록
    @PostMapping("/api/wellness-info/{wellnessInfoId}/likes")
    public ResponseEntity<Void> createLiked(@PathVariable("wellnessInfoId") Long wellnessInfoId){
        likedService.createLiked(wellnessInfoId);
        return ResponseEntity.ok().build();
    }

    // 좋아요 취소
    @DeleteMapping("/api/wellness-info/{wellnessInfoId}/likes")
    public ResponseEntity<Void> deleteLiked(@PathVariable("wellnessInfoId") Long wellnessInfoId){
        likedService.deleteLiked(wellnessInfoId);
        return ResponseEntity.ok().build();
    }

    // 좋아요 목록 조회
    @GetMapping("/api/my-trips/likes")
    public ResponseEntity<LikedResponse.LikedList> getLikedList(@RequestParam(required = false, defaultValue = "FOOD") String thema){
        return ResponseEntity.ok(likedService.LikedList(thema));
    }

}

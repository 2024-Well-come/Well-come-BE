package com.wellcome.WellcomeBE.domain.like.controller;

import com.wellcome.WellcomeBE.domain.like.service.LikedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

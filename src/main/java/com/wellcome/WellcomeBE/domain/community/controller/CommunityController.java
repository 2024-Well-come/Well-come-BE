package com.wellcome.WellcomeBE.domain.community.controller;

import com.wellcome.WellcomeBE.domain.community.dto.request.CommunityReviewRequest;
import com.wellcome.WellcomeBE.domain.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;


    // 후기 게시글 등록
    @PostMapping("/api/communities")
    public ResponseEntity<Void> createReviewPost(
            @RequestPart(value = "request") @Valid CommunityReviewRequest request,
            @RequestPart(value = "imgList", required = false) List<MultipartFile> imgList
    ){
        communityService.createReviewPost(request, imgList);
        return ResponseEntity.ok().build();
    }

}

package com.wellcome.WellcomeBE.domain.community.controller;

import com.wellcome.WellcomeBE.domain.community.dto.request.ReviewPostRequest;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostResponse;
import com.wellcome.WellcomeBE.domain.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;


    // 후기 게시글 등록
    @PostMapping("/api/communities")
    public ResponseEntity<Void> createReviewPost(
            @RequestPart(value = "request") @Valid ReviewPostRequest request,
            @RequestPart(value = "imgList", required = false) List<MultipartFile> imgList
    ){
        communityService.createReviewPost(request, imgList);
        return ResponseEntity.ok().build();
    }

    // 후기 게시글 목록 조회
    @GetMapping("/api/communities")
    public ResponseEntity<ReviewPostResponse> getReviewPostList(
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page
    ){
        return ResponseEntity.ok(communityService.getReviewPostList(sort, page));
    }

}

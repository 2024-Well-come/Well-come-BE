package com.wellcome.WellcomeBE.domain.community.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.community.dto.request.CommunityReviewRequest;
import com.wellcome.WellcomeBE.domain.communityImg.CommunityImg;
import com.wellcome.WellcomeBE.domain.communityImg.repository.CommunityImgRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.image.S3Service;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final TokenProvider tokenProvider;
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanPlaceRepository tripPlanPlaceRepository;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final S3Service s3Service;
    private final CommunityImgRepository communityImgRepository;
    private static final int MAX_IMG_COUNT = 10;

    /**
     * 후기 게시글 등록
     */
    @Transactional
    public void createReviewPost(CommunityReviewRequest request, List<MultipartFile> imgFileList) {

        // 사용자 인증
        Member currentMember = tokenProvider.getMember();

        // 여행 후기 게시글 작성
        TripPlan tripPlan = tripPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new CustomException(TRIP_PLAN_NOT_FOUND));

        // - 작성 권한 확인
        if(tripPlan.getMember().getId() != currentMember.getId()){
            throw new CustomException(ACCESS_DENIED, "해당 여행 폴더에 대해 후기 작성 권한이 없습니다.");
        }

        // - 해당 여행 폴더에 대해 후기가 이미 작성되었는지 확인
        communityRepository.findByTripPlan(tripPlan).ifPresent(community -> { throw new CustomException(COMMUNITY_ALREADY_EXISTS); });

        // - 게시글 등록
        Community community = communityRepository.save(Community.createByTripPlan(currentMember, tripPlan, request.getTitle(), request.getContent()));

        // 이미지 저장
        if(imgFileList != null && !imgFileList.isEmpty()){
            saveImgList(community, imgFileList);
        }

        // 장소 개별 후기 저장
        Optional.ofNullable(request.getReviewList())
                .ifPresent(reviewList -> reviewList.forEach(review -> {
                    WellnessInfo wellnessInfo = wellnessInfoRepository.findById(review.getWellnessInfoId())
                            .orElseThrow(() -> new CustomException(WELLNESS_INFO_NOT_FOUND));
                    TripPlanPlace tripPlanPlace = tripPlanPlaceRepository.findByTripPlanIdAndWellnessInfoId(tripPlan.getId(), wellnessInfo.getId())
                            .orElseThrow(() -> new CustomException(TRIP_PLAN_PLACE_NOT_FOUND, "해당 여행 폴더에 추가되지 않은 웰니스 정보입니다."));

                    tripPlanPlace.updatePlaceReview(review.getRating(), review.getReview());
                }));

    }

    /**
     * 이미지 저장
     */
    private void saveImgList(Community community, List<MultipartFile> imgFileList){
        // 이미지 개수 제한 확인
        if(imgFileList.size() > MAX_IMG_COUNT){
            throw new CustomException(IMG_LIMIT_EXCEEDED);
        }

        // 이미지 파일 S3에 업로드, DB에 객체 URL 저장
        List<CommunityImg> communityImgList = new ArrayList<>();
        imgFileList.stream().forEach(img -> {
            try {
                String imgUrl = s3Service.uploadImgFile(img);
                communityImgList.add(CommunityImg.create(community, imgUrl));
            } catch (IOException e) {
                log.error("MultipartFile 파일 업로드 중 오류 발생: {}", e.getMessage());
                throw new CustomException(IMG_UPLOAD_ERROR);
            }
        });
        communityImgRepository.saveAll(communityImgList);
    }
}

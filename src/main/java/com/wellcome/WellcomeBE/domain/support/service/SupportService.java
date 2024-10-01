package com.wellcome.WellcomeBE.domain.support.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.Support;
import com.wellcome.WellcomeBE.domain.support.dto.SupportRequest;
import com.wellcome.WellcomeBE.domain.support.repository.SupportRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SupportService {
    private final TokenProvider tokenProvider;
    private final SupportRepository supportRepository;
    private final CommunityRepository communityRepository;
    private final WellnessInfoRepository wellnessInfoRepository;


    // 커뮤니티 추천 로직 (단일 ID)
    public void createCommunitySupport(Long communityId) {
        Member member = tokenProvider.getMember();

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMUNITY_NOT_FOUND));

        supportRepository.findByMemberAndCommunityId(member, communityId)
                .orElseGet(() -> supportRepository.save(Support.createcommunitySupport(community, member)));

    }

    // 커뮤니티 삭제
    @Transactional
    public void deleteCommunitySupport(Long communityId) {
        Member member = tokenProvider.getMember();

        // 요청받은 communityId와 현재 사용자 ID로 Support 조회
        supportRepository.findByMemberAndCommunityId(member, communityId).ifPresent(
                supportRepository::delete);
    }

    @Transactional
    public void createTripPlanPlaceSupport(SupportRequest.CreateCommunityInWellnessSupportRequestDto req) {
        Community community = communityRepository.findById(req.getCommunityId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMUNITY_NOT_FOUND));
        Member member = tokenProvider.getMember();

        req.getWellnessInfoIds().forEach(wellness -> {
            // 웰니스 정보 존재 확인
            WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellness).orElseThrow(() -> new CustomException(CustomErrorCode.WELLNESS_INFO_NOT_FOUND));

            // Support 조회 및 생성
            supportRepository.findByMemberAndCommunityIdAndWellnessInfoId(member, req.getCommunityId() , wellness)
                    .orElseGet(() -> {
                        Support newSupport = Support.createWellnessInfoSupport(community, wellnessInfo, member);
                        return supportRepository.save(newSupport); // Support 생성 및 저장
                    });
        });
    }

    @Transactional
    public void deleteCommunityInWellnessSupport(SupportRequest.DeleteCommunityInWellnessSupportRequestDto req) {
        Member member = tokenProvider.getMember();

        // 특정 TripPlanPlace에 대해 Member와 CommunityId가 일치하는 Support 조회
        supportRepository.findByMemberAndCommunityIdAndWellnessInfoId(member, req.getCommunityId(), req.getWellnessInfoId())
                .ifPresent(
                        supportRepository::delete);
    }

}

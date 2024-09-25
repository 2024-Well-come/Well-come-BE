package com.wellcome.WellcomeBE.domain.support.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.Support;
import com.wellcome.WellcomeBE.domain.support.repository.SupportRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
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
    private final TripPlanPlaceRepository tripPlanPlaceRepository;

    @Transactional
    public void createSupport(List<Long> ids, String type) {
        Member member = tokenProvider.getMember();

        // COMMUNITY 타입 처리 (단일 ID)
        if (type.equals("COMMUNITY")) {
            if (ids == null || ids.size() != 1) {
                throw new CustomException(CustomErrorCode.SUPPORT_COMMUNITY_CNT);
            }
            createCommunitySupport(ids.get(0), member);
        }
        // TRIPPLANPLACE 타입 처리 (단일 또는 다중 ID)
        else if (type.equals("TRIP_PLAN_PLACE")) {
            if (ids == null || ids.isEmpty()) {
                throw new CustomException(CustomErrorCode.SUPPORT_TRIP_PLAN_PLACE);
            }
            createTripPlanPlaceSupport(ids, member);
        }
        else {
            throw new CustomException(CustomErrorCode.SUPPORT_TYPE_MISMATCH);
        }
    }

    // 커뮤니티 추천 로직 (단일 ID)
    private void createCommunitySupport(Long id, Member member) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMUNITY_NOT_FOUND));

        Support support = Support.builder()
                .member(member)
                .community(community)
                .build();

        supportRepository.save(support);
    }

    // TripPlanPlace 추천 로직 (단일 또는 다중 ID)
    private void createTripPlanPlaceSupport(List<Long> ids, Member member) {
        ids.forEach(id -> {
            TripPlanPlace tripPlanPlace = tripPlanPlaceRepository.findById(id)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.TRIP_PLAN_PLACE_NOT_FOUND));

            Support support = Support.builder()
                    .member(member)
                    .tripPlanPlace(tripPlanPlace)
                    .build();

            supportRepository.save(support);
        });
    }

    @Transactional
    public void deleteSupport(Long supportId, String type) {
        Support support = supportRepository.findById(supportId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SUPPORT_NOT_FOUND));

        boolean isValidType = switch (type) {
            case "COMMUNITY" -> support.getCommunity() != null;
            case "TRIP_PLAN_PLACE" -> support.getTripPlanPlace() != null;
            default -> false;
        };

        if (!isValidType) {
            throw new CustomException(CustomErrorCode.SUPPORT_TYPE_MISMATCH);
        }

        supportRepository.delete(support);
    }

}

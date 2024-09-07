package com.wellcome.WellcomeBE.domain.like.service;

import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.like.dto.response.LikedResponse;
import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.vo.LikeWellnessInfoVo;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.MEMBER_NOT_FOUND;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.WELLNESS_INFO_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikedService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final LikedRepository likedRepository;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final GoogleMapInfoService googleMapInfoService;

    /**
     * 좋아요 등록
     */
    public void createLiked(Long wellnessInfoId) {

        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(WELLNESS_INFO_NOT_FOUND));

        Member member = memberRepository.findById(tokenProvider.getMemberID())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        // 좋아요 내역이 없을 경우, 좋아요 등록
        likedRepository.findByMemberId(member.getId())
                .orElseGet(() -> likedRepository.save(Liked.create(wellnessInfo, member)));
    }

    /**
     * 좋아요 취소
     */
    public void deleteLiked(Long wellnessInfoId) {

        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(WELLNESS_INFO_NOT_FOUND));

        Member member = memberRepository.findById(tokenProvider.getMemberID())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        // 좋아요 내역이 있을 경우, 좋아요 삭제
        likedRepository.findByMemberId(member.getId())
                .ifPresent(likedRepository::delete);
    }

    /**
     * 좋아요 목록 조회
     * thema 전체 또는 각 테마 선책
     * 좋아요 담긴 thema 리스트 전달
     * 각 테마별 좋아요 목록 전달
     */
    public LikedResponse.LikedList LikedList(Thema thema){
        Member member = tokenProvider.getMember();

        // 조회할 테마 목록 설정
        List<Thema> themaList = new ArrayList<>();
        if (thema == null) {
            themaList = Thema.getThemaList();
        } else {
            themaList.add(thema);
            log.info(String.valueOf(thema));
            log.info(themaList.toString());

        }

        List<LikeWellnessInfoVo> likeWellnessInfoVo = likedRepository.findByMemberIdAndThemaIn(member, themaList);
        List<LikedResponse.WellnessInfoList> wellnessInfoList = likeWellnessInfoVo.stream()
                .map(vo -> {
                    // 각 웰니스 정보에 대한 Google Maps Place Details 호출
                    PlaceReviewResponse.PlaceResult placeDetails = googleMapInfoService.getPlaceDetails(vo.getPlaceId()).block().getResult();
                    return LikedResponse.WellnessInfoList.from(vo, placeDetails);
                })
                .collect(Collectors.toList());

        // 사용자가 좋아요한 테마 목록 가져오기
        List<Thema> themas = likedRepository.findLikedThemaByMember(member);

        // 최종 응답 객체 생성
        return LikedResponse.LikedList.builder()
                .themaList(themas)
                .wellnessInfoList(wellnessInfoList)
                .build();
    }
}

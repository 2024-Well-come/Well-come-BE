package com.wellcome.WellcomeBE.domain.community.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostDetailResponse;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.communityImg.CommunityImg;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.repository.SupportRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @InjectMocks
    private  CommunityService communityService;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private SupportRepository supportRepository;

    @Mock
    private WellnessInfoRepository wellnessInfoRepository;

    private Member member;
    private WellnessInfo wellnessInfo;
    @BeforeEach
    void setUp(){
        member = mock(Member.class);
        when(tokenProvider.getMember()).thenReturn(member);

    }

    @Test
    void getReviewPostDetail_후기_게시글_상세_조회_성공(){
        //given
        Long communityId = 1L;
        Community community = Mockito.mock(Community.class);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community)); // 커뮤니티 조회 성공
        when(community.getMember()).thenReturn(member);

        wellnessInfo = Mockito.mock(WellnessInfo.class);
        when(wellnessInfo.getThema()).thenReturn(Thema.STAY);

        String imgUrl = "https://Test_Mock_Images";
        java.util.List<CommunityImg> imgList = List.of(CommunityImg.create(community,imgUrl));
        when(community.getCommunityImgs()).thenReturn(imgList);


        List<Object[]> mockWellnessInfo = List.of(
                new Object[]{wellnessInfo, mock(TripPlanPlace.class), true, false}, // 지원한 경우
                new Object[]{wellnessInfo, mock(TripPlanPlace.class), false, false} // 지원하지 않은 경우
        );
        when(wellnessInfoRepository.findWellnessInfoWithSupportAndLikedByCommunityId(communityId, member))
                .thenReturn(mockWellnessInfo);


        //when
        when(supportRepository.existsByCommunityAndMember(community, member)).thenReturn(true);
        ReviewPostDetailResponse response1 = communityService.getReviewPostDetail(communityId);

        when(supportRepository.existsByCommunityAndMember(community, member)).thenReturn(false);
        ReviewPostDetailResponse response2 = communityService.getReviewPostDetail(communityId);


        //then
        assertNotNull(response1);
        assertEquals(2, response1.getWellnessInfoHistory().size());
        assertEquals(imgUrl, response1.getPostImgs().get(0));
        assertTrue(response1.getIsSupport());

        assertNotNull(response2);
        assertEquals(2, response1.getWellnessInfoHistory().size());
        assertEquals(imgUrl, response2.getPostImgs().get(0));
        assertFalse(response2.getIsSupport());

    }

    @Test
    void getReviewPostDetail_커뮤니티가_존재하지_않은_예외(){
        // given
        Long communityId = 99L;
        //when
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        CustomException exception= assertThrows(CustomException.class, () -> communityService.getReviewPostDetail(communityId)); // 예외 발생 검증
        assertEquals(CustomErrorCode.COMMUNITY_NOT_FOUND, exception.getCustomErrorCode());

    }

    @Test
    void getReviewPostDetail_커뮤니티_첨부된_이미지_없음(){
        Long communityId = 1L;
        Community mockCommunity = Mockito.mock(Community.class);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(mockCommunity));
        when(mockCommunity.getMember()).thenReturn(member);

        wellnessInfo = Mockito.mock(WellnessInfo.class);
        when(wellnessInfo.getThema()).thenReturn(Thema.STAY);

        Object[] wellnessInfoData = new Object[]{wellnessInfo, mock(TripPlanPlace.class), false, true};
        List<Object[]> mockWellnessInfo = Arrays.asList(new Object[][]{wellnessInfoData});

        when(supportRepository.existsByCommunityAndMember(mockCommunity, member)).thenReturn(true);
        when(mockCommunity.getCommunityImgs()).thenReturn(Collections.emptyList()); // 빈 리스트 반환
        when(wellnessInfoRepository.findWellnessInfoWithSupportAndLikedByCommunityId(communityId, member))
                .thenReturn(mockWellnessInfo);

        // when
        ReviewPostDetailResponse response = communityService.getReviewPostDetail(communityId);

        // then
        assertNotNull(response.getPostImgs());
        assertEquals(0, response.getPostImgs().size());

    }

}
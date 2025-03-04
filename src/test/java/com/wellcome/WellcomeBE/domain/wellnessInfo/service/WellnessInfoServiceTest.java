package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.Article.repository.ArticleRepository;
import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostResponse;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoReviewPostResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WellnessInfoServiceTest {

    @InjectMocks
    private WellnessInfoApiService wellnessInfoApiService;

    @Mock
    private WellnessInfoRepository wellnessInfoRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private GoogleMapInfoService googleMapInfoService;

    @Mock
    private LikedRepository likedRepository;

    @Mock
    private WellnessInfoImgRepository wellnessInfoImgRepository;

    @Mock
    private WellnessInfoService wellnessInfoService;

    @Mock
    private TripPlanPlaceRepository tripPlanPlaceRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Test
    void getWellnessInfoReviewPosts_후기_게시글_정상_조회(){
        // given
        long wellnessInfoId = 1000L;
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);;
        when(wellnessInfoRepository.findById(wellnessInfoId))
                .thenReturn(Optional.of(wellnessInfo));

        List<TripPlan> reviewedPlanList = List.of(Mockito.mock(TripPlan.class), Mockito.mock(TripPlan.class));
        when(tripPlanPlaceRepository.findByWellnessInfoAndReviewConditionsExist(wellnessInfo))
                .thenReturn(reviewedPlanList);

        // - 커뮤니티 게시글 생성
        List<Community> communityList = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            communityList.add(createTestCommunity(i+1));
        }

        int pageSize = 3;
        PageRequest pageRequest = PageRequest.of(0, pageSize);
        when(communityRepository.findByTripPlanInOrderByCreatedAtDesc(pageRequest, reviewedPlanList))
                .thenReturn(communityList.subList(0, pageSize));

        // when
        WellnessInfoReviewPostResponse response = wellnessInfoApiService.getWellnessInfoReviewPosts(wellnessInfoId);

        // then
        verify(communityRepository, times(1)).findByTripPlanInOrderByCreatedAtDesc(pageRequest, reviewedPlanList);
        assertEquals(pageSize, response.getReviewList().size());
        assertEquals("후기1", response.getReviewList().get(0).getTitle());
    }

    @Test
    void getWellnessInfoReviewPosts_해당_웰니스_정보를_포함한_여행폴더가_없는경우(){
        // given
        long wellnessInfoId = 1000L;
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);;
        when(wellnessInfoRepository.findById(wellnessInfoId))
                .thenReturn(Optional.of(wellnessInfo));

        // - 해당 웰니스 정보를 저장한 여행 폴더가 없도록 설정
        when(tripPlanPlaceRepository.findByWellnessInfoAndReviewConditionsExist(wellnessInfo))
                .thenReturn(Collections.emptyList());

        when(communityRepository.findByTripPlanInOrderByCreatedAtDesc(any(PageRequest.class), anyList()))
                .thenReturn(Collections.emptyList());

        // when
        WellnessInfoReviewPostResponse response = wellnessInfoApiService.getWellnessInfoReviewPosts(wellnessInfoId);

        // then
        assertNotNull(response);
        assertTrue(response.getReviewList().isEmpty());
    }

    @Test
    void getWellnessInfoReviewPosts_해당_웰니스_정보가_포함된_여행폴더의_후기가_작성되지_않은경우(){
        // given
        long wellnessInfoId = 1000L;
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);;
        when(wellnessInfoRepository.findById(wellnessInfoId))
                .thenReturn(Optional.of(wellnessInfo));

        List<TripPlan> reviewedPlanList = List.of(Mockito.mock(TripPlan.class), Mockito.mock(TripPlan.class));
        when(tripPlanPlaceRepository.findByWellnessInfoAndReviewConditionsExist(wellnessInfo))
                .thenReturn(reviewedPlanList);

        // - 여행 폴더에 대해 작성한 후기 게시글이 없도록 설정
        when(communityRepository.findByTripPlanInOrderByCreatedAtDesc(any(PageRequest.class), eq(reviewedPlanList)))
                .thenReturn(Collections.emptyList());

        // when
        WellnessInfoReviewPostResponse response = wellnessInfoApiService.getWellnessInfoReviewPosts(wellnessInfoId);

        // then
        assertNotNull(response);
        assertTrue(response.getReviewList().isEmpty());
    }

    private Community createTestCommunity(int idx){
        return Community.createByTripPlan(Mockito.mock(Member.class), Mockito.mock(TripPlan.class), "후기"+idx, "내용");
    }
}

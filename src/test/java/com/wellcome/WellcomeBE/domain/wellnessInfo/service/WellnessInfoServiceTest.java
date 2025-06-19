package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.Article.repository.ArticleRepository;
import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal.WellnessInfoWithLiked;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoGoogleReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoReviewPostResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Category;
import com.wellcome.WellcomeBE.global.type.ImgSavedType;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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


    /**
     * 웰니스 장소 추천 목록 조회
     */
    @Test
    void getWellnessInfoList_웰니스_장소_목록_정상_조회(){
        // given
        Member member = Mockito.mock(Member.class);
        when(tokenProvider.getMember()).thenReturn(member);

        // - request (themaList, sigunguList)
        List<Thema> themaList = new ArrayList<>(List.of(Thema.NATURE, Thema.STAY));
        List<Sigungu> sigunguList = List.of(Sigungu.DONGHAE, Sigungu.SOKCHO);
        WellnessInfoListRequest request = WellnessInfoListRequest.create(
                themaList, sigunguList, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-03")
        );

        // - likedThema
        List<Thema> likedThemaList = List.of(Thema.STAY);
        when(likedRepository.findThemaByMemberOrderByLikedCount(member))
                .thenReturn(likedThemaList);

        // - WellnessInfo Mock 데이터 생성
        List<WellnessInfoWithLiked> content = new ArrayList<>();
        for(int i = 0; i < 2; i++){
            WellnessInfo wellnessInfo = createTestWellnessInfo(themaList, i);
            content.add(new WellnessInfoWithLiked(wellnessInfo, false));
        }

        // - 페이징 설정
        int page = 0;
        Pageable pageable = PageRequest.of(page, 5);
        Page<WellnessInfoWithLiked> data = new PageImpl<>(content, pageable, content.size());

        when(wellnessInfoRepository.findWellnessInfoByThemaAndSigunguWithWeight(
                any(), eq(member), eq(themaList), eq(sigunguList), any(), any(), any()
        )).thenReturn(data);
        when(wellnessInfoRepository.findDistinctThema(themaList)).thenReturn(themaList);
        when(wellnessInfoRepository.findDistinctSigungu(sigunguList)).thenReturn(sigunguList);

        // when
        WellnessInfoResponse response = wellnessInfoApiService.getWellnessInfoList(page, request, ImgSavedType.S3);

        // then
        verify(likedRepository, times(1)).findThemaByMemberOrderByLikedCount(member);
        verify(wellnessInfoRepository, times(1)).findWellnessInfoByThemaAndSigunguWithWeight(
                any(), eq(member), eq(themaList), eq(sigunguList), any(), any(), any()
        );

        assertEquals(2, response.getWellnessInfoList().getTotalCount());
        assertFalse(response.getWellnessInfoList().isHasPrevious());
        assertFalse(response.getWellnessInfoList().isHasNext());
    }

    private WellnessInfo createTestWellnessInfo(List<Thema> sortedThemaList, int idx){
        WellnessInfo w = Mockito.mock(WellnessInfo.class);

        when(w.getThema()).thenReturn(idx >= sortedThemaList.size() ? null : sortedThemaList.get(idx));
        when(w.getId()).thenReturn((long) idx);
        when(w.getThumbnailUrl()).thenReturn("thumbnailUrl");
        when(w.getTitle()).thenReturn("title");
        when(w.getCategory()).thenReturn(Category.TOURIST_SPOT);
        when(w.getAddress()).thenReturn("address");
        when(w.getMapX()).thenReturn(0.0);
        when(w.getMapY()).thenReturn(0.0);

        return w;
    }

    @Test
    void sortThemaListByLikedOrder_테마_정렬_검증(){
        // given
        List<Thema> themaList = new ArrayList<>(List.of(Thema.NATURE, Thema.STAY, Thema.FOOD));
        List<Thema> likedThemaList = List.of(Thema.STAY);

        // when
        List<Thema> sortedThemaList = wellnessInfoApiService.sortThemaListByLikedOrder(themaList, likedThemaList);

        // then
        assertEquals(List.of(Thema.STAY, Thema.NATURE, Thema.FOOD), sortedThemaList);
    }

    /**
     * 웰니스 장소 상세 조회(3) - 구글 리뷰 조회
     */
    @Test
    void getWellnessInfoGoogleReviews_구글_리뷰_정상_조회(){
        // given
        long wellnessInfoId = 1000L;
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        when(wellnessInfoRepository.findById(wellnessInfoId))
                .thenReturn(Optional.ofNullable(wellnessInfo));

        // - 구글맵에 해당 웰니스 정보가 있도록 설정 (parentId != null)
        when(wellnessInfo.getParentId()).thenReturn("parentId");

        // - PlaceResult 설정: 평점(rating) o, 리뷰(reviews) o
        PlaceReviewResponse.PlaceResult placeResult = Mockito.mock(PlaceReviewResponse.PlaceResult.class);
        when(placeResult.getRating()).thenReturn(3.0);

        List<PlaceReviewResponse.PlaceResult.PlaceReview> reviews = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            PlaceReviewResponse.PlaceResult.PlaceReview review = Mockito.mock(PlaceReviewResponse.PlaceResult.PlaceReview.class);
            lenient().when(review.getAuthor_name()).thenReturn("user"+i);
            reviews.add(review);
        }
        when(placeResult.getReviews()).thenReturn(reviews);

        // - PlaceReviewResponse 설정
        PlaceReviewResponse placeReviewResponse = Mockito.mock(PlaceReviewResponse.class);
        when(placeReviewResponse.getResult()).thenReturn(placeResult);
        when(googleMapInfoService.getPlaceDetails(anyString()))
                .thenReturn(Mono.just(placeReviewResponse));

        // when
        WellnessInfoGoogleReviewResponse response = wellnessInfoApiService.getWellnessInfoGoogleReviews(wellnessInfoId);

        // then
        verify(googleMapInfoService, times(1)).getPlaceDetails(anyString());

        assertEquals(3.0, response.getRating());
        assertFalse(response.getReviewList().isEmpty());
        IntStream.range(0, response.getReviewList().size())
                .forEach(i -> assertEquals("user" + i, response.getReviewList().get(i).getAuthorName()));
    }

    @Test
    void getWellnessInfoGoogleReviews_구글맵에_해당_웰니스_정보가_없는경우(){
        // given
        long wellnessInfoId = 1000L;
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        when(wellnessInfoRepository.findById(wellnessInfoId))
                .thenReturn(Optional.ofNullable(wellnessInfo));

        // - 구글맵에 해당 웰니스 정보가 없도록 설정 (parentId = null)
        when(wellnessInfo.getParentId()).thenReturn(null);

        // when
        WellnessInfoGoogleReviewResponse response = wellnessInfoApiService.getWellnessInfoGoogleReviews(wellnessInfoId);

        // then
        assertNull(response.getRating());
        assertTrue(response.getReviewList().isEmpty());
    }

    @Test
    void getWellnessInfoGoogleReviews_구글맵에_해당_웰니스_정보가_있지만_리뷰_정보가_없는경우(){
        // given
        long wellnessInfoId = 1000L;
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        when(wellnessInfoRepository.findById(wellnessInfoId))
                .thenReturn(Optional.ofNullable(wellnessInfo));

        // - 구글맵에 해당 웰니스 정보가 있도록 설정 (parentId != null)
        when(wellnessInfo.getParentId()).thenReturn("parentId");

        // - PlaceResult 설정: 평점(rating) o, 리뷰(reviews) x
        PlaceReviewResponse.PlaceResult placeResult = Mockito.mock(PlaceReviewResponse.PlaceResult.class);
        when(placeResult.getRating()).thenReturn(3.0);
        when(placeResult.getReviews()).thenReturn(List.of());

        // - PlaceReviewResponse 설정
        PlaceReviewResponse placeReviewResponse = Mockito.mock(PlaceReviewResponse.class);
        when(placeReviewResponse.getResult()).thenReturn(placeResult);
        when(googleMapInfoService.getPlaceDetails(anyString()))
                .thenReturn(Mono.just(placeReviewResponse));

        // when
        WellnessInfoGoogleReviewResponse response = wellnessInfoApiService.getWellnessInfoGoogleReviews(wellnessInfoId);

        // then
        assertNotNull(placeResult);
        assertEquals(3.0, response.getRating());
        assertTrue(response.getReviewList().isEmpty());
    }

    /**
     * 웰니스 장소 상세 조회(4) - 후기 게시글 조회
     */
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

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
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.*;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.setup.WellnessInfoService;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
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
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    @Mock private WellnessInfo wellness;


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


    /**(1) 웰니스 기본 정보 조회
     * [정상 흐름] parentId, 이미지, 좋아요, 날씨 데이터 모두 존재하는 경우
     */
    @Test
    void getWellnessInfoBasic_allDataPresent_successfullyReturnsResponse() {
        // given
        Long id = 123L;
        String parentId = "place_abc123";

        // 1. wellnessInfo 존재
        when(wellnessInfoRepository.findById(id)).thenReturn(Optional.of(wellness));
        when(wellness.getParentId()).thenReturn(parentId);
        when(wellness.getSigungu()).thenReturn(Sigungu.DONGHAE);
        when(wellness.getCategory()).thenReturn(Category.EVENT);
        when(wellness.getTel()).thenReturn("031-989-8282");

        // 2. Google Place API mock
        PlaceReviewResponse placeReviewResponse = createTestPlaceReviewResponseWithReviews();
        when(googleMapInfoService.getPlaceDetails(parentId)).thenReturn(Mono.just(placeReviewResponse));

        // 3. 이미지 mock
        List<String> images = List.of("img1.jpg", "img2.jpg");
        when(wellnessInfoImgRepository.findByWellnessInfo(wellness)).thenReturn(images);

        // 4. 좋아요 여부 mock
        Member member = mock(Member.class);
        when(tokenProvider.getMember()).thenReturn(member);
        when(likedRepository.existsByWellnessInfoAndMember(wellness, member)).thenReturn(true);

        // 5. 날씨 정보 mock
        // 5. 날씨 정보 mock
        Map<String, String> weatherMap = Map.of(
                "SKY", "1",    // 맑음
                "PTY", "0",    // 강수 없음
                "T1H", "25.0"  // 온도
        );
        WeatherResponse weather = WeatherResponse.from(weatherMap);

        // wellness.getSigungu()가 실제로 사용할 값 설정
        when(wellness.getSigungu()).thenReturn(Sigungu.DONGHAE);
        when(wellness.getMapX()).thenReturn(128.0);
        when(wellness.getMapY()).thenReturn(37.5);

        // fetchWeatherInfo stub: 실제 객체 호출 X, 직접 지정된 weather 반환
        when(wellnessInfoService.fetchWeatherInfo(anyInt(), anyInt())).thenReturn(weather);


        // 6. 뷰 카운트 업데이트 무시
        doNothing().when(wellness).updateViewNum();

        // when
        WellnessInfoBasicResponse response = wellnessInfoApiService.getWellnessInfoBasic(id);


        // then
        assertThat(response).isNotNull();
        assertThat(response.getImgList()).containsExactly("img1.jpg", "img2.jpg");
        assertThat(response.getIsLiked()).isTrue();
        assertThat(response.getOpenDetail()).isNotBlank();
        assertThat(response.getTel()).isNotNull();
        assertThat(response.getWebsite()).isNotNull();
        assertThat(response.getWeather()).isEqualTo(weather);

        // verify 의존 객체 호출 및 인자 검증
        verify(wellnessInfoRepository).findById(id);
        verify(googleMapInfoService).getPlaceDetails(parentId);
        verify(wellnessInfoImgRepository).findByWellnessInfo(wellness);
        verify(likedRepository).existsByWellnessInfoAndMember(eq(wellness), isNotNull());
        verify(wellnessInfoService).fetchWeatherInfo(anyInt(), anyInt());
        verify(wellness).updateViewNum();
    }


    /**
     * [분기 처리] parentId가 null인 경우, Google Place API를 호출하지 않아야 함
     */
    @Test
    void getWellnessInfoBasic_whenParentIdIsNull_shouldSkipPlaceApiCall() {
        // given
        Long id = 456L;
        when(wellnessInfoRepository.findById(id)).thenReturn(Optional.of(wellness));
        when(wellness.getParentId()).thenReturn(null); // parentId가 null
        when(wellness.getSigungu()).thenReturn(Sigungu.SOKCHO);
        when(wellness.getCategory()).thenReturn(Category.EVENT);
        when(wellnessInfoImgRepository.findByWellnessInfo(wellness)).thenReturn(List.of());
        Member member = Mockito.mock(Member.class);
        when(tokenProvider.getMember()).thenReturn(member);
        lenient().when(likedRepository.existsByWellnessInfoAndMember(wellness, member)).thenReturn(false);

        WeatherResponse weather = WeatherResponse.builder().temperature("22.0").build();
        lenient().when(wellnessInfoService.fetchWeatherInfo(anyInt(), anyInt())).thenReturn(weather);
        doNothing().when(wellness).updateViewNum();

        // when
        WellnessInfoBasicResponse response = wellnessInfoApiService.getWellnessInfoBasic(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOpenDetail()).isEqualTo("정보 없음");
        assertThat(response.getWebsite()).isEqualTo("");
        assertThat(response.getTel()).isNull();
        assertThat(response.getIsOpen()).isNull();

        // Google API는 호출되지 않아야 함
        verify(googleMapInfoService, never()).getPlaceDetails(anyString());
    }

    /**
     * [예외 처리] wellnessInfoId에 해당하는 데이터가 존재하지 않는 경우 예외 발생
     */
    @Test
    void getWellnessInfoBasic_whenWellnessNotFound_shouldThrowException() {
        // given
        Long id = 999L;
        when(wellnessInfoRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wellnessInfoApiService.getWellnessInfoBasic(id))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.WELLNESS_INFO_NOT_FOUND.getMessage());

        // 다른 의존 객체들은 호출되지 않아야 함
        verifyNoInteractions(googleMapInfoService, wellnessInfoImgRepository, likedRepository, wellnessInfoService);
    }

    @Test
    void getWellnessInfoUsefulInformation_success() {
        // given: 기본 데이터 설정
        Long wellnessInfoId = 1L;
        WellnessInfo wellnessInfo = WellnessInfo.builder()
                .id(wellnessInfoId)
                .mapX(127.0)
                .mapY(37.5)
                .thema(Thema.STAY)
                .category(Category.ACCOMMODATION)
                .sigungu(Sigungu.CHEORWON)
                .build();

        // 3km 내에서 찾은 장소가 3개뿐이라 부족 → 10km 반경으로 재조회되도록 설정
        List<WellnessInfoNearResponse.WellnessNearbyDto> nearbyIn3Km = List.of(
                createNearbyDto("placeId1", 1.2),
                createNearbyDto("placeId2", 2.3),
                createNearbyDto("placeId3", 2.9)
        );

        List<WellnessInfoNearResponse.WellnessNearbyDto> nearbyIn10Km = List.of(
                createNearbyDto("placeId1", 1.2),
                createNearbyDto("placeId2", 2.3),
                createNearbyDto("placeId3", 2.9),
                createNearbyDto("placeId4", 5.5),
                createNearbyDto("placeId5", 8.1),
                createNearbyDto("placeId6", 9.9)
        );

        // placeDetails 응답 모킹
        PlaceReviewResponse placeDetailsResponse = new PlaceReviewResponse();
        PlaceReviewResponse.PlaceResult result = new PlaceReviewResponse.PlaceResult();
        placeDetailsResponse.setResult(result);

        // 각 리포지토리 및 서비스의 동작을 정의
        when(wellnessInfoRepository.findById(wellnessInfoId)).thenReturn(Optional.of(wellnessInfo));
        when(wellnessInfoRepository.findNearbyWithinRadius(127.0, 37.5, wellnessInfoId, 3.0, 6))
                .thenReturn(nearbyIn3Km); // 3km 결과
        when(wellnessInfoRepository.findNearbyWithinRadius(127.0, 37.5, wellnessInfoId, 10.0, 6))
                .thenReturn(nearbyIn10Km); // 10km 결과
        when(googleMapInfoService.getPlaceDetails(any()))
                .thenReturn(Mono.just(placeDetailsResponse)); // 장소 상세 정보 응답
        when(articleRepository.findTop5ByWellnessInfoOrderByCreatedAt(wellnessInfo))
                .thenReturn(List.of()); // 아티클은 없는 경우

        // when: 실제 서비스 호출
        WellnessInfoUsefulInformationResponse response = wellnessInfoApiService.getWellnessInfoUsefulInformation(wellnessInfoId);

        // then: 결과 검증
        assertNotNull(response); // 응답이 null이 아님
        assertEquals(6, response.getNearbyList().size()); // 최종 근처 장소 6개
        assertEquals(0, response.getArticleList().size()); // 아티클 없음

        // 리포지토리 및 서비스 메서드 호출 검증
        verify(wellnessInfoRepository).findById(wellnessInfoId);
        verify(wellnessInfoRepository, times(2)).findNearbyWithinRadius(anyDouble(), anyDouble(), eq(wellnessInfoId), anyDouble(), eq(6));
        verify(googleMapInfoService, times(6)).getPlaceDetails(any());
        verify(articleRepository).findTop5ByWellnessInfoOrderByCreatedAt(wellnessInfo);
    }

    /**
     * 모킹된 NearbyDto 생성
     */
    private WellnessInfoNearResponse.WellnessNearbyDto createNearbyDto(String placeId, double distance) {
        WellnessInfoNearResponse.WellnessNearbyDto dto = mock(WellnessInfoNearResponse.WellnessNearbyDto.class);
        lenient().when(dto.getPlaceId()).thenReturn(placeId);
        lenient().when(dto.getDistance()).thenReturn(distance);
        lenient().when(dto.getThema()).thenReturn(Thema.STAY);
        lenient().when(dto.getCategory()).thenReturn(Category.ACCOMMODATION);

        return dto;
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

    // 평점과 리뷰가 포함된 PlaceReviewResponse 생성
    private PlaceReviewResponse createTestPlaceReviewResponseWithReviews() {
        PlaceReviewResponse.PlaceResult placeResult = new PlaceReviewResponse.PlaceResult();
        placeResult.setRating(4.5);
        placeResult.setUser_ratings_total(3);
        placeResult.setName("테스트 장소");
        placeResult.setRating(4.5);
        placeResult.setUser_ratings_total(3);
        placeResult.setWebsite("http://place.example.com");

        List<PlaceReviewResponse.PlaceResult.PlaceReview> reviews = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PlaceReviewResponse.PlaceResult.PlaceReview review = new PlaceReviewResponse.PlaceResult.PlaceReview();
            review.setAuthor_name("작성자" + i);
            review.setText("리뷰 내용 " + i);
            review.setRating(5);
            reviews.add(review);
        }
        placeResult.setReviews(reviews);

        // PlaceReviewResponse 생성
        PlaceReviewResponse placeReviewResponse = new PlaceReviewResponse();
        placeReviewResponse.setResult(placeResult);

        return placeReviewResponse;
    }

}

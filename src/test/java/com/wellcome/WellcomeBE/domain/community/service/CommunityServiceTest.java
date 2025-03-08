package com.wellcome.WellcomeBE.domain.community.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.CommunityTestHelper;
import com.wellcome.WellcomeBE.domain.community.dto.request.ReviewPostRequest;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostResponse;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.communityImg.CommunityImg;
import com.wellcome.WellcomeBE.domain.communityImg.repository.CommunityImgRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.repository.SupportRepository;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.image.S3Service;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.domain.community.CommunityTestHelper.*;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @InjectMocks
    private CommunityService communityService;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TripPlanRepository tripPlanRepository;

    @Mock
    private TripPlanPlaceRepository tripPlanPlaceRepository;

    @Mock
    private WellnessInfoRepository wellnessInfoRepository;

    @Mock
    private SupportRepository supportRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private CommunityImgRepository communityImgRepository;

    private static final long memberId = 1L;
    private static final long tripPlanId = 10L;


    /**
     * 후기 게시글 등록
     */
    @Test
    void createReviewPost_게시글과_장소개별후기_정상_등록() {
        // given
        // 커뮤니티 게시글 o + 장소 개별 후기 o + 이미지 x
        Member member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn(memberId);
        when(tokenProvider.getMember()).thenReturn(member);

        TripPlan tripPlan = createTestTripPlan(member);
        ReviewPostRequest request = createReviewPostRequest(tripPlan.getId(), true);

        when(tripPlanRepository.findById(request.getPlanId()))
                .thenReturn(Optional.of(tripPlan)); //작성 권한 o
        when(communityRepository.findByTripPlan(tripPlan))
                .thenReturn(Optional.empty()); //이미 작성된 후기 x

        // - 장소 개별 후기 o
        List<ReviewPostRequest.UserReview> reviewList = request.getReviewList();
        List<TripPlanPlace> places = new ArrayList<>();

        reviewList.forEach(r -> {
            WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
            when(wellnessInfo.getId()).thenReturn(r.getWellnessInfoId());

            TripPlanPlace place = createTestTripPlanPlace(wellnessInfo, tripPlan);
            places.add(place);

            when(wellnessInfoRepository.findById(r.getWellnessInfoId()))
                    .thenReturn(Optional.of(wellnessInfo));
            when(tripPlanPlaceRepository.findByTripPlanIdAndWellnessInfoId(tripPlan.getId(), r.getWellnessInfoId()))
                    .thenReturn(Optional.of(place));
        });

        // when
        communityService.createReviewPost(request, null); //이미지 x

        // then
        // - 커뮤니티 게시글 저장 여부 검증
        verify(communityRepository, times(1)).save(any(Community.class));

        // - 장소 개별 후기 저장 여부 검증
        for(int i = 0; i < places.size(); i++){
            assertEquals(reviewList.get(i).getRating(), places.get(i).getRating());
            assertEquals(reviewList.get(i).getReview(), places.get(i).getReview());
        }
        assertFalse(tripPlan.getIsActive());

        // - 이미지 저장 로직 실행 여부 검증
        verify(communityImgRepository, never()).saveAll(any());
    }

    @Test
    void createReviewPost_게시글과_이미지_정상_등록() {
        // given
        // 커뮤니티 게시글 o + 장소 개별 후기 x + 이미지 o
        CommunityService spyService = spy(communityService);

        Member member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn(memberId);
        when(tokenProvider.getMember()).thenReturn(member);

        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getMember()).thenReturn(member);

        ReviewPostRequest request = Mockito.spy(createReviewPostRequest(tripPlan.getId(), false));

        when(tripPlanRepository.findById(request.getPlanId()))
                .thenReturn(Optional.of(tripPlan)); //작성 권한 o
        when(communityRepository.findByTripPlan(tripPlan))
                .thenReturn(Optional.empty()); //이미 작성된 후기 x

        // - 장소 개별 후기 x
        doReturn(null).when(request).getReviewList(); //장소 개별 후기 x

        // - 게시글 등록
        Community savedCommunity = mock(Community.class);
        when(communityRepository.save(any(Community.class))).thenReturn(savedCommunity);

        // - 이미지 o
        List<MultipartFile> imgFileList = List.of(mock(MultipartFile.class), mock(MultipartFile.class));
        doNothing().when(spyService).saveImgList(any(Community.class), anyList());

        // when
        spyService.createReviewPost(request, imgFileList);

        // then
        // - 커뮤니티 게시글 저장 여부 검증
        verify(communityRepository, times(1)).save(any(Community.class));

        // - 이미지 저장 로직 실행 여부 검증
        verify(spyService, times(1)).saveImgList(any(Community.class), anyList());
    }

    @Test
    void createReviewPost_작성_권한이_없는_경우_예외발생(){
        // given
        Member folderOwner = Mockito.mock(Member.class);
        when(folderOwner.getId()).thenReturn(memberId);

        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getMember()).thenReturn(folderOwner);
        when(tripPlanRepository.findById(tripPlanId)).thenReturn(Optional.of(tripPlan));

        Member anotherMember = Mockito.mock(Member.class);
        when(anotherMember.getId()).thenReturn(2L);
        when(tokenProvider.getMember()).thenReturn(anotherMember);

        ReviewPostRequest request = mock(ReviewPostRequest.class);
        when(request.getPlanId()).thenReturn(tripPlanId);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                communityService.createReviewPost(request, null)
        );

        // then
        assertEquals(ACCESS_DENIED, exception.getCustomErrorCode());
    }

    @Test
    void createReviewPost_해당_여행폴더에_대한_후기_게시글을_이미_작성한_경우_예외발생(){
        // given
        Member member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn(memberId);
        when(tokenProvider.getMember()).thenReturn(member);

        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getMember()).thenReturn(member);

        ReviewPostRequest request = mock(ReviewPostRequest.class);
        when(request.getPlanId()).thenReturn(tripPlanId);

        when(tripPlanRepository.findById(request.getPlanId()))
                .thenReturn(Optional.of(tripPlan));
        when(communityRepository.findByTripPlan(tripPlan))
                .thenReturn(Optional.of(Mockito.mock(Community.class)));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                communityService.createReviewPost(request, null)
        );

        // then
        assertEquals(COMMUNITY_ALREADY_EXISTS, exception.getCustomErrorCode());
    }

    @Test
    void saveImgList_이미지_정상_등록() throws IOException {
        // given
        Community community = Mockito.mock(Community.class);

        List<MultipartFile> imgFileList = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            imgFileList.add(mock(MultipartFile.class));
        }

        // - 이미지 Url 설정
        when(s3Service.uploadImgFile(any(MultipartFile.class)))
                .thenReturn("imgUrl1", "imgUrl2", "imgUrl3");

        // when
        communityService.saveImgList(community, imgFileList);

        // then
        verify(s3Service, times(3)).uploadImgFile(any(MultipartFile.class));

        ArgumentCaptor<List<CommunityImg>> captor = ArgumentCaptor.forClass(List.class);
        verify(communityImgRepository, times(1)).saveAll(captor.capture());

        List<CommunityImg> savedImgs = captor.getValue();
        assertEquals(3, savedImgs.size());
        assertEquals("imgUrl1", savedImgs.get(0).getImgUrl());
        assertEquals("imgUrl2", savedImgs.get(1).getImgUrl());
        assertEquals("imgUrl3", savedImgs.get(2).getImgUrl());
    }

    @Test
    void saveImgList_이미지_개수_초과시_예외발생() throws IOException {
        // given
        Community community = Mockito.mock(Community.class);

        List<MultipartFile> imgFileList = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            imgFileList.add(mock(MultipartFile.class));
        }

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            communityService.saveImgList(community, imgFileList);
        });

        // then
        assertEquals(IMG_LIMIT_EXCEEDED, exception.getCustomErrorCode());

        verify(s3Service, never()).uploadImgFile(any(MultipartFile.class));
        verify(communityImgRepository, never()).saveAll(anyList());
    }

    @Test
    void saveImgList_IOException_발생시_커스텀_예외발생() throws IOException {
        // given
        Community community = Mockito.mock(Community.class);
        List<MultipartFile> imgFileList = List.of(mock(MultipartFile.class));

        // - 강제로 IOException 발생시키기
        when(s3Service.uploadImgFile(any(MultipartFile.class)))
                .thenThrow(new IOException("이미지 저장 실패"));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            communityService.saveImgList(community, imgFileList);
        });

        // then
        assertEquals(IMG_UPLOAD_ERROR, exception.getCustomErrorCode());

        verify(communityImgRepository, never()).saveAll(anyList());
    }

    /**
     * 후기 게시글 목록 조회
     */
    @Test
    void getReviewPostList_기본_최신순_조회(){
        // given
        // - 커뮤니티 생성
        List<Community> communityList = new ArrayList<>();
        LocalDateTime date = LocalDateTime.of(2025, 1, 15, 0, 0, 0);
        for(int i = 0; i < 5; i++){
            Community community = Mockito.mock(Community.class);
            lenient().when(community.getCreatedAt()).thenReturn(date.minusDays(i));
            communityList.add(community);
        }

        // - [웰컴인들이 공감해요] 조회 (추천순)
        PageRequest recommendPageRequest = PageRequest.of(0, 2);
        Page<Community> resultPage1 = new PageImpl<>(communityList.subList(0, 2), recommendPageRequest, communityList.size());
        when(communityRepository.findByPostTypeOrderBySupportCount(any(PageRequest.class), eq(Community.PostType.TRIP_PLAN)))
                .thenReturn(resultPage1);

        // - [전체글] 조회 (최신순)
        String sort = "latest";
        int page = 0;
        int pageSize = 3;
        PageRequest reviewPageRequest = PageRequest.of(page, pageSize);
        Page<Community> resultPage2 = new PageImpl<>(communityList.subList(page * pageSize, pageSize), reviewPageRequest, communityList.size());
        when(communityRepository.findByPostTypeOrderByCreatedAtDesc(any(PageRequest.class), eq(Community.PostType.TRIP_PLAN)))
                .thenReturn(resultPage2);

        // when
        ReviewPostResponse response = communityService.getReviewPostList(sort, page);

        // then
        assertEquals(communityList.size(), response.getReviewList().getTotalCount());
        assertEquals(pageSize, response.getReviewList().getData().size());

        assertFalse(response.getReviewList().isHasPrevious());
        assertTrue(response.getReviewList().isHasNext());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy년 MM월 dd일");
        List<LocalDate> actualDates = response.getReviewList().getData().stream()
                        .map(ReviewPostResponse.ReviewPostBrief::getPostDate)
                        .map(d -> LocalDate.parse(d, formatter))
                        .collect(Collectors.toList());
        List<LocalDate> expectedDates = actualDates.stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
        assertEquals(expectedDates, actualDates);
    }

}
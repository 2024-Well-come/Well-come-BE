package com.wellcome.WellcomeBE.domain.tripPlanPlace.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceCreateRequest;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.ACCESS_DENIED;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TRIP_PLAN_PLACE_NOT_IN_FOLDER;
import static com.wellcome.WellcomeBE.utils.TestObjectFactory.createTestTripPlanPlace;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPlanPlaceServiceTest {

    @InjectMocks
    private TripPlanPlaceService tripPlanPlaceService;

    @Mock
    private WellnessInfoRepository wellnessInfoRepository;

    @Mock
    private TripPlanRepository tripPlanRepository;

    @Mock
    private TripPlanPlaceRepository tripPlanPlaceRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Captor
    private ArgumentCaptor<List<TripPlanPlace>> tripPlanPlaceCaptor;

    private Member member;
    private final long memberId = 1L;


    @BeforeEach
    void setup(){
        member = Mockito.mock(Member.class);
    }

    @Test
    void deleteTripPlanPlace_권한없는_사용자가_삭제_요청시_예외발생() {
        // given
        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getId()).thenReturn(100L);

        TripPlanPlace place1 = Mockito.mock(TripPlanPlace.class);
        when(place1.getId()).thenReturn(10L);
        TripPlanPlace place2 = Mockito.mock(TripPlanPlace.class);
        when(place2.getId()).thenReturn(20L);

        List<Long> tripPlanPlaceIdList = List.of(place1.getId(), place2.getId());

        // - 해당 폴더에 권한 없는 사용자 생성
        Member anotherMember = Mockito.mock(Member.class);
        when(anotherMember.getId()).thenReturn(2L);
        when(tokenProvider.getMember()).thenReturn(anotherMember);

        // TripPlan을 찾을 수 없도록 설정
        when(tripPlanRepository.findByIdAndMemberId(tripPlan.getId(), anotherMember.getId()))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tripPlanPlaceService.deleteTripPlanPlace(tripPlan.getId(), new TripPlanPlaceDeleteRequest(tripPlanPlaceIdList))
        );

        // then
        assertEquals(ACCESS_DENIED, exception.getCustomErrorCode());
    }

    @Test
    void deleteTripPlanPlace_요청한_여행폴더에_속하지_않는_여행지_삭제시_예외발생() {
        // given
        when(member.getId()).thenReturn(memberId);
        when(tokenProvider.getMember()).thenReturn(member);

        // - 여행 폴더, 여행 폴더 내 여행지 생성
        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getId()).thenReturn(100L);
        when(tripPlanRepository.findByIdAndMemberId(tripPlan.getId(), member.getId()))
                .thenReturn(Optional.of(tripPlan));

        TripPlanPlace place1 = Mockito.mock(TripPlanPlace.class);
        when(place1.getId()).thenReturn(10L);
        when(place1.getTripPlan()).thenReturn(tripPlan);

        TripPlanPlace place2 = Mockito.mock(TripPlanPlace.class);
        when(place2.getId()).thenReturn(20L);
        when(place2.getTripPlan()).thenReturn(tripPlan);

        // - 요청된 여행 폴더에 속하지 않는 여행지 추가
        TripPlan anotherTripPlan = Mockito.mock(TripPlan.class);
        TripPlanPlace place3 = Mockito.mock(TripPlanPlace.class);
        when(place3.getId()).thenReturn(30L);
        when(place3.getTripPlan()).thenReturn(anotherTripPlan);

        // - 삭제 요청 여행지: 여러 여행 폴더의 여행지 혼합해서 구성
        List<Long> tripPlanPlaceIdList = List.of(place1.getId(), place2.getId(), place3.getId());
        when(tripPlanPlaceRepository.findByIdIn(tripPlanPlaceIdList))
                .thenReturn(List.of(place1, place2, place3));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> tripPlanPlaceService.deleteTripPlanPlace(tripPlan.getId(), new TripPlanPlaceDeleteRequest(tripPlanPlaceIdList)));

        // then
        assertEquals(TRIP_PLAN_PLACE_NOT_IN_FOLDER, exception.getCustomErrorCode());
    }

    @Test
    void deleteTripPlanPlace_요청한_여행폴더에_여행지가_모두_속하면_성공() {
        // given
        when(member.getId()).thenReturn(memberId);
        when(tokenProvider.getMember()).thenReturn(member);

        // - 여행 폴더, 여행 폴더 내 여행지 생성
        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getId()).thenReturn(100L);
        when(tripPlanRepository.findByIdAndMemberId(tripPlan.getId(), member.getId()))
                .thenReturn(Optional.of(tripPlan));

        TripPlanPlace place1 = Mockito.mock(TripPlanPlace.class);
        when(place1.getId()).thenReturn(10L);
        when(place1.getTripPlan()).thenReturn(tripPlan);

        TripPlanPlace place2 = Mockito.mock(TripPlanPlace.class);
        when(place2.getId()).thenReturn(20L);
        when(place2.getTripPlan()).thenReturn(tripPlan);

        List<Long> tripPlanPlaceIdList = List.of(place1.getId(), place2.getId());
        when(tripPlanPlaceRepository.findByIdIn(tripPlanPlaceIdList))
                .thenReturn(List.of(place1, place2));

        // - 커뮤니티에 참조되지 않는 여행폴더로 설정
        when(communityRepository.findByTripPlan(tripPlan))
                .thenReturn(Optional.empty());

        // when
        assertDoesNotThrow(() -> tripPlanPlaceService.deleteTripPlanPlace(tripPlan.getId(), new TripPlanPlaceDeleteRequest(tripPlanPlaceIdList)));

        // then
        verify(tripPlanPlaceRepository).deleteAllByIdInBatch(tripPlanPlaceIdList);
    }

    @Test
    void deleteTripPlanPlace_요청한_여행폴더가_커뮤니티에서_참조되면_비활성화() {
        // given
        when(member.getId()).thenReturn(memberId);
        when(tokenProvider.getMember()).thenReturn(member);

        // - 여행 폴더, 여행 폴더 내 여행지 생성
        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getId()).thenReturn(100L);
        when(tripPlanRepository.findByIdAndMemberId(tripPlan.getId(), member.getId()))
                .thenReturn(Optional.of(tripPlan));

        TripPlanPlace place1 = createTestTripPlanPlace(Mockito.mock(WellnessInfo.class), tripPlan);
        ReflectionTestUtils.setField(place1, "id", 10L);
        TripPlanPlace place2 = createTestTripPlanPlace(Mockito.mock(WellnessInfo.class), tripPlan);
        ReflectionTestUtils.setField(place2, "id", 20L);

        List<Long> tripPlanPlaceIdList = List.of(place1.getId(), place2.getId());
        when(tripPlanPlaceRepository.findByIdIn(tripPlanPlaceIdList))
                .thenReturn(List.of(place1, place2));

        // - 커뮤니티에 참조되는 여행폴더로 설정
        when(communityRepository.findByTripPlan(tripPlan))
                .thenReturn(Optional.of(Mockito.mock(Community.class)));

        // when
        assertDoesNotThrow(() -> tripPlanPlaceService.deleteTripPlanPlace(tripPlan.getId(), new TripPlanPlaceDeleteRequest(tripPlanPlaceIdList)));

        // then
        // - delete 호출 x
        verify(tripPlanPlaceRepository, never()).deleteAllByIdInBatch(any());

        // - 삭제 요청된 여행지를 비활성화 처리
        verify(tripPlanPlaceRepository).saveAll(List.of(place1, place2));
        assertEquals(TripPlan.Status.INACTIVE, place1.getStatus());
        assertEquals(TripPlan.Status.INACTIVE, place2.getStatus());
    }

    @Test
    void createTripPlanPlace_여행폴더_여행지_추가하기(){
        // given
        Long planId = 1L;
        Long wellnessId = 100L;
        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        TripPlanPlaceCreateRequest request = mock(TripPlanPlaceCreateRequest.class);
        when(request.getWellnessInfoId()).thenReturn(wellnessId);


        when(wellnessInfoRepository.findById(wellnessId)).thenReturn(Optional.of(wellnessInfo));
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));
        when(tokenProvider.getMember()).thenReturn(member);
        when(tripPlanPlaceRepository.existsByTripPlanAndWellnessInfoAndMember(tripPlan, wellnessInfo, member)).thenReturn(false);

        // when
        tripPlanPlaceService.createTripPlanPlace(planId, request);

        // then
        verify(tripPlanPlaceRepository, times(1)).save(any(TripPlanPlace.class));
    }

    @Test
    void createTripPlanPlace_웰니스_X() {
        // given
        Long wellnessId = 1L;
        Long planId = 99L;
        TripPlanPlaceCreateRequest request = mock(TripPlanPlaceCreateRequest.class);
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        when(request.getWellnessInfoId()).thenReturn(wellnessId);
        when(wellnessInfoRepository.findById(wellnessId)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tripPlanPlaceService.createTripPlanPlace(planId, request)
        );

        // then
        assertEquals(CustomErrorCode.WELLNESS_INFO_NOT_FOUND, exception.getCustomErrorCode());
    }

    @Test
    void createTripPlanPlace_TripPlan_X() {
        // given
        Long wellnessId = 1L;
        Long planId = 99L;
        TripPlanPlaceCreateRequest request = mock(TripPlanPlaceCreateRequest.class);
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);

        when(request.getWellnessInfoId()).thenReturn(wellnessId);
        when(wellnessInfoRepository.findById(wellnessId)).thenReturn(Optional.of(wellnessInfo));
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> tripPlanPlaceService.createTripPlanPlace(planId, request));
        // then
        assertEquals(CustomErrorCode.TRIP_PLAN_NOT_FOUND, exception.getCustomErrorCode());

    }

    @Test
    void createTripPlanPlace_shouldThrow_whenDuplicatePlace() {
        // given
        Long planId = 1L;
        Long wellnessId = 100L;
        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        TripPlanPlaceCreateRequest request = mock(TripPlanPlaceCreateRequest.class);
        when(request.getWellnessInfoId()).thenReturn(wellnessId);

        when(wellnessInfoRepository.findById(wellnessId)).thenReturn(Optional.of(wellnessInfo));
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));
        when(tokenProvider.getMember()).thenReturn(member);
        when(tripPlanPlaceRepository.existsByTripPlanAndWellnessInfoAndMember(tripPlan, wellnessInfo, member)).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> tripPlanPlaceService.createTripPlanPlace(planId, request));
        assertEquals(CustomErrorCode.TRIP_PLAN_PLACE_DUPLICATION, exception.getCustomErrorCode());
    }
}
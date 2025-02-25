package com.wellcome.WellcomeBE.domain.tripPlan.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Role;
import com.wellcome.WellcomeBE.global.type.SocialLogin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.ACCESS_DENIED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripPlanServiceTest {

    @InjectMocks
    private TripPlanService tripPlanService;

    @Mock
    private TripPlanRepository tripPlanRepository;

    @Mock
    private TripPlanPlaceRepository tripPlanPlaceRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private GoogleMapInfoService googleMapInfoService;

    @Captor
    private ArgumentCaptor<List<TripPlan>> tripPlanCaptor;

    @Test
    void deleteTripPlan_저장된_여행지가_없고_커뮤니티_미참조폴더_삭제() {
        // given
        Member currentMember = createTestMember("user1", 12345L);
        ReflectionTestUtils.setField(currentMember, "id", 1L);
        when(tokenProvider.getMember()).thenReturn(currentMember);

        // - 여행 폴더 1개 생성
        TripPlan tripPlan = createTestTripPlan("folder1", currentMember);
        ReflectionTestUtils.setField(tripPlan, "id", 100L);

        // 1. 해당 폴더에 저장된 여행지가 없는 상태로 설정
        when(tripPlanRepository.findByIdIn(List.of(tripPlan.getId())))
                .thenReturn(List.of(tripPlan));

        // 2. 커뮤니티에 참조되지 않는 여행 폴더로 설정
        when(communityRepository.findReferencedTripPlanIds(List.of(tripPlan.getId())))
                .thenReturn(List.of());

        // When
        assertDoesNotThrow(() -> tripPlanService.deleteTripPlan(new TripPlanDeleteRequest(List.of(tripPlan.getId()))));

        // Then
        verify(tripPlanPlaceRepository).deleteAllInBatch(Collections.emptyList());
        verify(tripPlanRepository).deleteAllByIdInBatch(List.of(tripPlan.getId()));
    }

    @Test
    void deleteTripPlan_권한없는_사용자가_삭제_요청시_예외발생(){
        // given
        // - 삭제하려는 여행 폴더 (여행 폴더 생성자: folderOwner)
        Member folderOwner = createTestMember("user1", 12345L);
        ReflectionTestUtils.setField(folderOwner, "id", 1L);

        TripPlan tripPlan = Mockito.mock(TripPlan.class);
        when(tripPlan.getId()).thenReturn(100L);
        when(tripPlan.getMember()).thenReturn(folderOwner);

        // - 해당 폴더 삭제 요청하는 다른 사용자 (anotherMember)
        Member anotherMember = createTestMember("user2", 67890L);
        ReflectionTestUtils.setField(anotherMember, "id", 2L);

        // 삭제 요청
        List<Long> tripPlanIdList = List.of(tripPlan.getId());
        when(tripPlanRepository.findByIdIn(tripPlanIdList))
                .thenReturn(List.of(tripPlan));
        when(tokenProvider.getMember())
                .thenReturn(anotherMember);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tripPlanService.deleteTripPlan(new TripPlanDeleteRequest(tripPlanIdList))
        );

        // then
        assertEquals(ACCESS_DENIED, exception.getCustomErrorCode());
    }

    @Test
    void deleteTripPlan_여러_폴더_삭제(){
        // given
        Member currentMember = createTestMember("user1", 12345L);
        ReflectionTestUtils.setField(currentMember, "id", 1L);
        when(tokenProvider.getMember()).thenReturn(currentMember);

        // - 여행 폴더 여러 개 생성
        TripPlan tripPlan1 = createTestTripPlan("folder1", currentMember);
        ReflectionTestUtils.setField(tripPlan1, "id", 100L);

        TripPlan tripPlan2 = createTestTripPlan("folder2", currentMember);
        ReflectionTestUtils.setField(tripPlan2, "id", 200L);

        // - 여행 폴더(tripPlan2) 내 여행지 추가
        WellnessInfo wellnessInfo1 = Mockito.mock(WellnessInfo.class);
        WellnessInfo wellnessInfo2 = Mockito.mock(WellnessInfo.class);
        TripPlanPlace place1 = createTestTripPlanPlace(wellnessInfo1, tripPlan2);
        TripPlanPlace place2 = createTestTripPlanPlace(wellnessInfo2, tripPlan2);

        // 1. 삭제할 여행 폴더 목록 설정
        List<Long> tripPlanIdList = List.of(tripPlan1.getId(), tripPlan2.getId());
        when(tripPlanRepository.findByIdIn(tripPlanIdList))
                .thenReturn(List.of(tripPlan1, tripPlan2));

        // 2. 요청된 여행 폴더 모두 커뮤니티에 참조되지 않도록 설정
        when(communityRepository.findReferencedTripPlanIds(tripPlanIdList))
                .thenReturn(List.of());

        List<Long> notReferencedTripPlanIds = tripPlanIdList;
        when(tripPlanPlaceRepository.findByTripPlanIdIn(notReferencedTripPlanIds))
                .thenReturn(List.of(place1, place2));

        // when
        assertDoesNotThrow(() -> tripPlanService.deleteTripPlan(new TripPlanDeleteRequest(tripPlanIdList)));

        // then
        verify(tripPlanPlaceRepository).deleteAllInBatch(List.of(place1, place2));
        verify(tripPlanRepository).deleteAllByIdInBatch(tripPlanIdList);
    }

    @Test
    void deleteTripPlan_커뮤니티_참조폴더_삭제() {
        // given
        Member currentMember = createTestMember("user1", 12345L);
        ReflectionTestUtils.setField(currentMember, "id", 1L);
        when(tokenProvider.getMember()).thenReturn(currentMember);

        // - 커뮤니티에 참조되는 여행 폴더 생성
        TripPlan tripPlan = createTestTripPlan("folder1", currentMember);
        ReflectionTestUtils.setField(tripPlan, "id", 100L);
        Community.createByTripPlan(currentMember, tripPlan, "title", "content");

        // 커뮤니티에 참조되는 여행 폴더로 설정
        when(communityRepository.findReferencedTripPlanIds(List.of(tripPlan.getId())))
                .thenReturn(List.of(tripPlan.getId()));
        when(tripPlanRepository.findByIdIn(List.of(tripPlan.getId())))
                .thenReturn(List.of(tripPlan));

        // when
        assertDoesNotThrow(() -> tripPlanService.deleteTripPlan(new TripPlanDeleteRequest(List.of(tripPlan.getId()))));

        // then
        verify(tripPlanRepository).saveAll(tripPlanCaptor.capture());
        List<TripPlan> changedTripPlans = tripPlanCaptor.getValue();
        assertEquals(TripPlan.Status.INACTIVE, changedTripPlans.get(0).getStatus());
    }

    @Test
    void deleteTripPlan_커뮤니티_참조폴더_미참조폴더_혼합_삭제() {
        // given
        Member currentMember = createTestMember("user1", 12345L);
        ReflectionTestUtils.setField(currentMember, "id", 1L);
        when(tokenProvider.getMember()).thenReturn(currentMember);

        // - 커뮤니티에 참조되는 여행 폴더 생성
        TripPlan tripPlan1 = createTestTripPlan("folder1", currentMember);
        ReflectionTestUtils.setField(tripPlan1, "id", 100L);
        Community.createByTripPlan(currentMember, tripPlan1, "title", "content");

        // - 커뮤니티에 참조되지 않는 여행 폴더 생성, 여행지 추가
        TripPlan tripPlan2 = createTestTripPlan("folder2", currentMember);
        ReflectionTestUtils.setField(tripPlan2, "id", 200L);

        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        TripPlanPlace place = createTestTripPlanPlace(wellnessInfo, tripPlan2);
        when(tripPlanPlaceRepository.findByTripPlanIdIn(List.of(tripPlan2.getId())))
                .thenReturn(List.of(place));

        // 1. 삭제할 여행 폴더 목록 설정
        List<Long> tripPlanIdList = List.of(tripPlan1.getId(), tripPlan2.getId());
        when(tripPlanRepository.findByIdIn(tripPlanIdList))
                .thenReturn(List.of(tripPlan1, tripPlan2));

        // 2. 커뮤니티에 참조되는 여행 폴더로 설정
        List<Long> referencedTripPlanIds = List.of(tripPlan1.getId());
        when(communityRepository.findReferencedTripPlanIds(tripPlanIdList))
                .thenReturn(referencedTripPlanIds);
        when(tripPlanRepository.findByIdIn(referencedTripPlanIds))
                .thenReturn(List.of(tripPlan1));

        // 3. 커뮤니티에 참조되지 않는 여행 폴더로 설정
        List<Long> notReferencedTripPlanIds = List.of(tripPlan2.getId());
        when(tripPlanPlaceRepository.findByTripPlanIdIn(notReferencedTripPlanIds))
                .thenReturn(List.of(place));

        // when
        assertDoesNotThrow(() -> tripPlanService.deleteTripPlan(new TripPlanDeleteRequest(tripPlanIdList)));

        // then
        // - 커뮤니티에 참조되는 여행 폴더: status 변경 (ACTIVE -> INACTIVE)
        verify(tripPlanRepository).saveAll(tripPlanCaptor.capture());
        List<TripPlan> changedTripPlans = tripPlanCaptor.getValue();
        assertEquals(TripPlan.Status.INACTIVE, changedTripPlans.get(0).getStatus());

        // - 커뮤니티에 참조되지 않는 여행 폴더: 여행지 삭제 -> 여행 폴더 삭제
        verify(tripPlanPlaceRepository).deleteAllInBatch(List.of(place));
        verify(tripPlanRepository).deleteAllByIdInBatch(List.of(tripPlan2.getId()));
    }

    private Member createTestMember(String nickname, Long kakaoId){
        return Member.builder()
                .nickname(nickname)
                .profileImg("profileImgUrl")
                .role(Role.USER)
                .socialType(SocialLogin.KAKAO)
                .kakaoId(kakaoId)
                .build();
    }

    private TripPlan createTestTripPlan(String folderName, Member member){
        return TripPlan.builder()
                .title(folderName)
                .startDate(LocalDate.parse("2025-01-01"))
                .endDate(LocalDate.parse("2025-01-05"))
                .member(member)
                .isActive(true)
                .status(TripPlan.Status.ACTIVE)
                .build();
    }

    private TripPlanPlace createTestTripPlanPlace(WellnessInfo wellnessInfo, TripPlan tripPlan){
        return TripPlanPlace.builder()
                .tripPlan(tripPlan)
                .wellnessInfo(wellnessInfo)
                .build();
    }
}
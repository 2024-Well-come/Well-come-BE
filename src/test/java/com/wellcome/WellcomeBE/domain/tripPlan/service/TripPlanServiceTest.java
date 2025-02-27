package com.wellcome.WellcomeBE.domain.tripPlan.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanDetailResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Role;
import com.wellcome.WellcomeBE.global.type.SocialLogin;

import com.wellcome.WellcomeBE.global.type.Thema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.ACCESS_DENIED;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TRIP_PLAN_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private Thema thema;

    @Test
    void getTripPlan_존재하지_않는_여행폴더_조회시_예외발생(){
        // given
        when(tokenProvider.getMember()).thenReturn(Mockito.mock(Member.class));

        Long tripPlanId = 100L;
        when(tripPlanRepository.findById(tripPlanId))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tripPlanService.getTripPlan(tripPlanId, null, 0));

        // then
        assertEquals(TRIP_PLAN_NOT_FOUND, exception.getCustomErrorCode());
    }

    @Test
    void getTripPlan_권한없는_사용자가_조회시_예외발생(){
        // given
        Member folderOwner = Mockito.mock(Member.class);
        when(folderOwner.getId()).thenReturn(1L);

        Member anotherMember = Mockito.mock(Member.class);
        when(anotherMember.getId()).thenReturn(2L);
        when(tokenProvider.getMember()).thenReturn(anotherMember);

        TripPlan tripPlan = createTestTripPlan("folder1", folderOwner);
        ReflectionTestUtils.setField(tripPlan, "id", 100L);
        when(tripPlanRepository.findById(tripPlan.getId()))
                .thenReturn(Optional.of(tripPlan));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tripPlanService.getTripPlan(tripPlan.getId(), null, 0));

        // then
        assertEquals(ACCESS_DENIED, exception.getCustomErrorCode());
    }

    @Test
    void getTripPlan_특정_테마_필터링(){
        // given
        Member member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(tokenProvider.getMember()).thenReturn(member);

        TripPlan tripPlan = createTestTripPlan("folder1", member);
        ReflectionTestUtils.setField(tripPlan, "id", 100L);
        when(tripPlanRepository.findById(tripPlan.getId()))
                .thenReturn(Optional.of(tripPlan));

        // - 여행 폴더 내 여행지 생성, 테마 설정
        List<TripPlanPlace> tripPlanPlaces = new ArrayList<>();
        List<TripPlanPlace> filteredTripPlanPlaces = new ArrayList<>();

        Thema selectedThema = Thema.NATURE;
        for(int i = 0; i < 10; i++){
            TripPlanPlace place = Mockito.mock(TripPlanPlace.class);
            WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);

            lenient().when(place.getId()).thenReturn((long) (10 * (i+1)));
            lenient().when(place.getWellnessInfo()).thenReturn(wellnessInfo);

            Thema thema = (i % 2 == 0) ? selectedThema : Thema.STAY;
            lenient().when(wellnessInfo.getThema()).thenReturn(thema);

            tripPlanPlaces.add(place);

            if(thema == selectedThema){
                filteredTripPlanPlaces.add(place);
            }
        }

        // - 페이징 설정
        int pageNum = 0, pageSize = 3;
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);

        Page<TripPlanPlace> filteredPage = new PageImpl<>(
                filteredTripPlanPlaces.subList(0, Math.min(pageSize, filteredTripPlanPlaces.size())),
                pageRequest,
                filteredTripPlanPlaces.size()
        );
        when(tripPlanPlaceRepository.findByTripPlanIdAndThema(any(PageRequest.class), eq(tripPlan.getId()), eq(selectedThema)))
                .thenReturn(filteredPage);

        // when
        TripPlanDetailResponse.SavedWellnessInfoList response =
                tripPlanService.getTripPlan(tripPlan.getId(), selectedThema, pageNum).getWellnessInfoList();

        // then
        assertNotNull(response);
        assertEquals(filteredPage.getSize(), response.getData().size());
        assertEquals(5, response.getTotalCount());
        assertTrue(
                response.getData().stream()
                    .allMatch(w -> w.getThema().equals(selectedThema.getName()))
        );

        assertFalse(response.isHasPrevious());
        assertTrue(response.isHasNext());
    }

    @Test
    void getTripPlan_선택된_테마가_없는경우_모든_테마의_여행지_가져오기(){
        // given
        Member member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(tokenProvider.getMember()).thenReturn(member);

        TripPlan tripPlan = createTestTripPlan("folder1", member);
        ReflectionTestUtils.setField(tripPlan, "id", 100L);
        when(tripPlanRepository.findById(tripPlan.getId()))
                .thenReturn(Optional.of(tripPlan));

        // - 여행 폴더 내 여행지 생성, 테마 설정
        List<TripPlanPlace> tripPlanPlaces = new ArrayList<>();

        for(int i = 0; i < 10; i++){
            TripPlanPlace place = Mockito.mock(TripPlanPlace.class);
            WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);

            lenient().when(place.getId()).thenReturn((long) (10 * (i+1)));
            lenient().when(place.getWellnessInfo()).thenReturn(wellnessInfo);

            Thema thema;
            if(i % 3 == 0)          thema = Thema.NATURE;
            else if(i % 3 == 1)     thema = Thema.STAY;
            else                    thema = Thema.FOOD;
            lenient().when(wellnessInfo.getThema()).thenReturn(thema);

            tripPlanPlaces.add(place);
        }

        // - 페이징 설정
        int pageNum = 0, pageSize = 3;
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);

        Page<TripPlanPlace> page = new PageImpl<>(
                tripPlanPlaces.subList(0, Math.min(pageSize, tripPlanPlaces.size())),
                pageRequest,
                tripPlanPlaces.size()
        );
        when(tripPlanPlaceRepository.findByTripPlanIdAndThema(any(PageRequest.class), eq(tripPlan.getId()), isNull()))
                .thenReturn(page);

        // when
        TripPlanDetailResponse totalResponse = tripPlanService.getTripPlan(tripPlan.getId(), null, pageNum);
        TripPlanDetailResponse.SavedWellnessInfoList response = totalResponse.getWellnessInfoList();

        // then
        assertNotNull(response);
        assertEquals(page.getSize(), response.getData().size());
        assertEquals(10, response.getTotalCount());

        List<Thema> themaList = totalResponse.getThemaList();
        assertThat(themaList).containsExactlyInAnyOrder(Thema.NATURE, Thema.STAY, Thema.FOOD);
    }

    @Test
    void getTripPlan_웰니스_정보의_parentId가_없는경우_googleMapInfoService_호출되지_않음(){
        // given
        Member member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(tokenProvider.getMember()).thenReturn(member);

        TripPlan tripPlan = createTestTripPlan("folder1", member);
        ReflectionTestUtils.setField(tripPlan, "id", 100L);
        when(tripPlanRepository.findById(tripPlan.getId()))
                .thenReturn(Optional.of(tripPlan));

        // - 여행 폴더 내 여행지 생성, parentId = null
        TripPlanPlace place = Mockito.mock(TripPlanPlace.class);
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        when(place.getWellnessInfo()).thenReturn(wellnessInfo);
        when(wellnessInfo.getParentId()).thenReturn(null); //parentId가 null인 경우
        when(wellnessInfo.getThema()).thenReturn(Thema.NATURE);

        List<TripPlanPlace> tripPlanPlaces = List.of(place);
        Page<TripPlanPlace> page = new PageImpl<>(tripPlanPlaces);
        when(tripPlanPlaceRepository.findByTripPlanIdAndThema(any(PageRequest.class), eq(tripPlan.getId()), isNull()))
                .thenReturn(page);

        // when
        tripPlanService.getTripPlan(tripPlan.getId(), null, 0);

        // then
        verify(googleMapInfoService, never()).getPlaceDetails(anyString());
    }

    @Test
    void getTripPlan_웰니스_정보의_parentId가_있는경우_googleMapInfoService_호출됨(){
        // given
        Member member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(tokenProvider.getMember()).thenReturn(member);

        TripPlan tripPlan = createTestTripPlan("folder1", member);
        ReflectionTestUtils.setField(tripPlan, "id", 100L);
        when(tripPlanRepository.findById(tripPlan.getId()))
                .thenReturn(Optional.of(tripPlan));

        // - 여행 폴더 내 여행지 생성, parentId != null
        TripPlanPlace place = Mockito.mock(TripPlanPlace.class);
        WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
        String mockParentId = "parentId";

        when(place.getWellnessInfo()).thenReturn(wellnessInfo);
        when(wellnessInfo.getParentId()).thenReturn(mockParentId); //parentId가 존재하는 경우
        when(wellnessInfo.getThema()).thenReturn(Thema.NATURE);

        when(googleMapInfoService.getPlaceDetails(mockParentId))
                .thenReturn(Mono.just(new PlaceReviewResponse()));

        List<TripPlanPlace> tripPlanPlaces = List.of(place);
        Page<TripPlanPlace> page = new PageImpl<>(tripPlanPlaces);
        when(tripPlanPlaceRepository.findByTripPlanIdAndThema(any(PageRequest.class), eq(tripPlan.getId()), isNull()))
                .thenReturn(page);

        // when
        tripPlanService.getTripPlan(tripPlan.getId(), null, 0);

        // then
        verify(googleMapInfoService, times(1)).getPlaceDetails(anyString());
    }


    @Test
    void deleteTripPlan_저장된_여행지가_없고_커뮤니티_미참조폴더_삭제(){
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
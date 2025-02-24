package com.wellcome.WellcomeBE.domain.support.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.Support;
import com.wellcome.WellcomeBE.domain.support.dto.SupportRequest;
import com.wellcome.WellcomeBE.domain.support.repository.SupportRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Optional;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportRepository supportRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private WellnessInfoRepository wellnessInfoRepository;
    @Mock
    private TokenProvider tokenProvider;
    @InjectMocks
    private SupportService supportService;
    private Long communityId;
    private Member member;
    private Support support;
    private Community community;

    @BeforeEach
    void setUp1() {
        communityId = 1L;

        member = Member.builder()
                .nickname("테스트 사용자")
                .kakaoId(2L)
                .build();
        community = Community.builder()
                .id(communityId)
                .member(member)
                .view(3L)
                .build();

        support = Support.createcommunitySupport(community, member);
        // ID 강제 설정
        ReflectionTestUtils.setField(support, "id", 100L);

        when(tokenProvider.getMember()).thenReturn(member);


    }
    @Test
    @DisplayName("커뮤니티 추천 등록1")
    public void 커뮤니티_존재하지_않을_경우(){
        // stub
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> supportService.createCommunitySupport(communityId));

        //then 예외가 발생했을 때, 올바른 에러 코드인지 확인
        assertEquals(CustomErrorCode.COMMUNITY_NOT_FOUND, exception.getCustomErrorCode());
    }



    @Test
    @DisplayName("커뮤니티 추천 등록2")
    public void 이미_Support_존재할_경우_새로운_support_생성_X(){

        // given: 커뮤니티를 stub으로 설정 -> BeforeEach가 있는데도 오류가 생겨서 추가함
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        //stub
       when(supportRepository.findByMemberAndCommunityId(member,communityId)).thenReturn(Optional.of(support));

       //when
        supportService.createCommunitySupport(communityId);

        // then
        verify(supportRepository, never()).save(any(Support.class));
    }

    @Test
    @DisplayName("커뮤니티 추천 등록3")
    public void Support_존재하지_않을_경우_새로운_Support_저장(){
        // Stub - repository Mock 설정
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        // When - 실제 동작 실행
        supportService.createCommunitySupport(communityId);

        // Then - Support가 생성되었는지 검증
        verify(supportRepository, times(1)).save(any(Support.class));
    }

    @Test
    @DisplayName("커뮤니티 추천 취소")
    public void Support_취소(){
        //stub
        when(supportRepository.findByMemberAndCommunityId(member, communityId)).thenReturn(Optional.of(support));

        // when
        supportService.deleteCommunitySupport(communityId);

        // Then
        verify(supportRepository).delete(support);
        assertFalse(supportRepository.findById(support.getId()).isPresent()); // 삭제된 후 null 검증

    }


    private SupportRequest.CreateCommunityInWellnessSupportRequestDto requestDto;
    private WellnessInfo wellnessInfo;

    @BeforeEach
    void setUp2(){
        requestDto=SupportRequest.CreateCommunityInWellnessSupportRequestDto.builder()
                .communityId(1L)
                .wellnessInfoId(10L)
                .build();
        wellnessInfo = WellnessInfo.builder()
                .id(10L)
                .build();

    }


    @Test
    @DisplayName("후기 게시글 내 장소 추천 추가1")
    public void 웰니스정보가_존재하지_않을_경우_예외_발생(){
        // stub
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        // when
        when(wellnessInfoRepository.findById(10L)).thenReturn(Optional.empty());
        CustomException exception = assertThrows(CustomException.class,
                ()->supportService.createTripPlanPlaceSupport(requestDto));

        // then 기대값,실제값
        assertEquals(CustomErrorCode.WELLNESS_INFO_NOT_FOUND, exception.getCustomErrorCode());

    }

    @Test
    @DisplayName("후기 게시글 내 장소 추천 추가2")
    public void Support_이미_존재할_경우_새로운_Support_생성_X(){
        //given
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(wellnessInfoRepository.findById(10L)).thenReturn(Optional.of(wellnessInfo));
        when(supportRepository.findByMemberAndCommunityIdAndWellnessInfoId(member, communityId , 10L))
                .thenReturn(Optional.of(support));

        //when
        supportService.createTripPlanPlaceSupport(requestDto);
        //then
        verify(supportRepository, never()).save(any(Support.class));

    }

    @Test
    @DisplayName("후기 게시글 내 장소 추천 추가3")
    public void Support_존재하지_않을_경우_새로운_Support_생성(){
        // given
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(wellnessInfoRepository.findById(10L)).thenReturn(Optional.of(wellnessInfo));

        when(supportRepository.findByMemberAndCommunityIdAndWellnessInfoId(member, communityId , 10L))
                .thenReturn(Optional.empty());
        // when
        supportService.createTripPlanPlaceSupport(requestDto);

        // then
        verify(supportRepository).save(any(Support.class));
    }

    @Test
    @DisplayName("후기 게시글 내 장소 추천 취소")
    public void 후기_게시글_내_장소_추천_취소(){
        // given
        SupportRequest.DeleteCommunityInWellnessSupportRequestDto requestDto1 = SupportRequest.DeleteCommunityInWellnessSupportRequestDto.builder()
                        .wellnessInfoId(10L)
                                .communityId(1L)
                                        .build();

        given(supportRepository.findByMemberAndCommunityIdAndWellnessInfoId(member, requestDto1.getCommunityId(), requestDto.getWellnessInfoId()))
                .willReturn(Optional.of(support));

        // when
        supportService.deleteCommunityInWellnessSupport(requestDto1);

        // then
        verify(supportRepository).delete(support);
        assertFalse(supportRepository.findById(support.getId()).isPresent());
    }


}
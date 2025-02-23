package com.wellcome.WellcomeBE.domain.support.service;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.Support;
import com.wellcome.WellcomeBE.domain.support.repository.SupportRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportRepository supportRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private TokenProvider tokenProvider;
    @InjectMocks
    private SupportService supportService;
    private Long communityId;
    private Member member;
    private Support support;
    private Community community;

    @BeforeEach
    void setUp() {
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
        assertFalse(supportRepository.findById(communityId).isPresent()); // 삭제된 후 null 검증

    }


}
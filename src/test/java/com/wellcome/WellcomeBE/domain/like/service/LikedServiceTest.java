package com.wellcome.WellcomeBE.domain.like.service;

import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.MEMBER_NOT_FOUND;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.WELLNESS_INFO_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikedServiceTest {
    @InjectMocks
    private LikedService likedService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private LikedRepository likedRepository;

    @Mock
    private WellnessInfoRepository wellnessInfoRepository;

    @Mock
    private GoogleMapInfoService googleMapInfoService;

    private Member member;
    private WellnessInfo wellnessInfo;
    private final long memberId = 1L;
    private final long wellnessInfoId = 1000L;

    @BeforeEach
    void setUp(){
        member = Mockito.mock(Member.class);
        wellnessInfo = Mockito.mock(WellnessInfo.class);
    }

    @Test
    void createLiked_좋아요가_없으면_새로운_좋아요_생성() {
        // given
        when(member.getId()).thenReturn(memberId);
        when(wellnessInfo.getId()).thenReturn(wellnessInfoId);

        when(wellnessInfoRepository.findById(wellnessInfo.getId()))
                .thenReturn(Optional.of(wellnessInfo));
        when(tokenProvider.getMemberID())
                .thenReturn(memberId);
        when(memberRepository.findById(member.getId()))
                .thenReturn(Optional.of(member));

        // 기존 좋아요가 없는 상태 설정
        when(likedRepository.findByMemberIdAndWellnessInfoId(memberId, wellnessInfoId))
                .thenReturn(Optional.empty());

        // when
        likedService.createLiked(wellnessInfoId);

        // then
        verify(likedRepository).save(any(Liked.class));
    }

    @Test
    void createLiked_존재하지_않는_WellnessInfo_예외발생() {
        // given
        Long invalidWellnessInfoId = 2000L;
        when(wellnessInfoRepository.findById(invalidWellnessInfoId))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                likedService.createLiked(invalidWellnessInfoId)
        );

        // then
        assertEquals(WELLNESS_INFO_NOT_FOUND, exception.getCustomErrorCode());
    }

    @Test
    void createLiked_존재하지_않는_Member_예외발생() {
        // given
        when(wellnessInfo.getId()).thenReturn(wellnessInfoId);

        Long invalidMemberId = 2L;
        when(wellnessInfoRepository.findById(wellnessInfo.getId()))
                .thenReturn(Optional.ofNullable(wellnessInfo));
        when(tokenProvider.getMemberID())
                .thenReturn(invalidMemberId);
        when(memberRepository.findById(invalidMemberId))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                likedService.createLiked(wellnessInfoId)
        );

        // then
        assertEquals(MEMBER_NOT_FOUND, exception.getCustomErrorCode());
    }

    @Test
    void createLiked_좋아요가_있으면_save_호출되지_않음() {
        // given
        when(member.getId()).thenReturn(memberId);
        when(wellnessInfo.getId()).thenReturn(wellnessInfoId);

        when(wellnessInfoRepository.findById(wellnessInfo.getId()))
                .thenReturn(Optional.of(wellnessInfo));
        when(tokenProvider.getMemberID())
                .thenReturn(memberId);
        when(memberRepository.findById(member.getId()))
                .thenReturn(Optional.of(member));

        // 좋아요 내역이 있는 상태로 설정
        Liked liked = Liked.create(wellnessInfo, member);
        when(likedRepository.findByMemberIdAndWellnessInfoId(memberId, wellnessInfoId))
                .thenReturn(Optional.of(liked));

        // when
        likedService.createLiked(wellnessInfoId);

        // then
        verify(likedRepository, never()).save(any(Liked.class));
    }

    @Test
    void deleteLiked_좋아요가_있으면_해당_좋아요_삭제() {
        // given
        when(member.getId()).thenReturn(memberId);
        when(wellnessInfo.getId()).thenReturn(wellnessInfoId);

        when(wellnessInfoRepository.findById(wellnessInfo.getId()))
                .thenReturn(Optional.of(wellnessInfo));
        when(tokenProvider.getMemberID())
                .thenReturn(memberId);
        when(memberRepository.findById(member.getId()))
                .thenReturn(Optional.of(member));

        // 좋아요가 있는 상태 설정
        Liked liked = Mockito.mock(Liked.class);
        when(likedRepository.findByMemberIdAndWellnessInfoId(memberId, wellnessInfoId))
                .thenReturn(Optional.of(liked));

        // when
        likedService.deleteLiked(wellnessInfoId);

        // then
        verify(likedRepository).delete(liked);
    }

    @Test
    void deleteLiked_좋아요가_없으면_delete_호출되지_않음() {
        // given
        when(member.getId()).thenReturn(memberId);
        when(wellnessInfo.getId()).thenReturn(wellnessInfoId);

        when(wellnessInfoRepository.findById(wellnessInfo.getId()))
                .thenReturn(Optional.of(wellnessInfo));
        when(tokenProvider.getMemberID())
                .thenReturn(memberId);
        when(memberRepository.findById(member.getId()))
                .thenReturn(Optional.of(member));

        // 좋아요 내역이 없는 상태 설정
        when(likedRepository.findByMemberIdAndWellnessInfoId(memberId, wellnessInfoId))
                .thenReturn(Optional.empty());

        // when
        likedService.deleteLiked(wellnessInfoId);

        // then
        verify(likedRepository, never()).delete(any());
    }
}
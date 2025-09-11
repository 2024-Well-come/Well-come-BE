package com.wellcome.WellcomeBE.domain.like.service;

import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.like.dto.response.LikedResponse;
import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.review.service.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.dto.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.vo.LikeWellnessInfoVo;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;

import com.wellcome.WellcomeBE.global.type.Thema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import reactor.core.publisher.Mono;

import java.util.List;
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
    @Test
    void LikedList_Thema_전체_조회_null() {
        // given
        int page = 0;
        Member mockMember = mock(Member.class);
        when(tokenProvider.getMember()).thenReturn(mockMember);

        // Thema가 null인 경우: 전체 Thema 목록 사용
        List<Thema> allThemas = Thema.getThemaList(); // 실제 내용이 들어 있다고 가정
        Pageable pageable = PageRequest.of(page, 6, Sort.by("createdAt").descending());

        // Like VO mock
        LikeWellnessInfoVo vo1 = mock(LikeWellnessInfoVo.class);
        when(vo1.getPlaceId()).thenReturn("place-id-1");
        when(vo1.getThema()).thenReturn(Thema.NATURE);

        Page<LikeWellnessInfoVo> likePage = new PageImpl<>(List.of(vo1), pageable, 1);

        // Thema.getThemaList() static mocking
        try (MockedStatic<Thema> themaMock = mockStatic(Thema.class)) {
            themaMock.when(Thema::getThemaList).thenReturn(allThemas);

            // Repository 및 외부 API mocking
            when(likedRepository.findByMemberIdAndThemaIn(eq(mockMember), eq(allThemas), any(Pageable.class)))
                    .thenReturn(likePage);

            PlaceReviewResponse.PlaceResult placeResult = mock(PlaceReviewResponse.PlaceResult.class);
            PlaceReviewResponse placeReviewResponse = mock(PlaceReviewResponse.class);
            when(placeReviewResponse.getResult()).thenReturn(placeResult);
            when(googleMapInfoService.getPlaceDetails("place-id-1")).thenReturn(Mono.just(placeReviewResponse));

            when(likedRepository.findLikedThemaByMember(mockMember))
                    .thenReturn(List.of(Thema.NATURE, Thema.HEALING_MEDITATION));

            // when
            LikedResponse.LikedList result = likedService.LikedList(null, page);

            // then
            assertEquals(1, result.getTotalCount());
            assertEquals(0, result.getPageNum());
            assertFalse(result.isHasPrevious());
            assertFalse(result.isHasNext());
            assertEquals(1, result.getWellnessInfoList().size());
            assertEquals(List.of(Thema.NATURE, Thema.HEALING_MEDITATION), result.getThemaList());
        }
    }

    @Test
    void LikedList_특정_Thema_조회() {
        // given
        int page = 0;
        Thema thema = Thema.STAY;
        Member mockMember = mock(Member.class);
        when(tokenProvider.getMember()).thenReturn(mockMember);

        Pageable pageable = PageRequest.of(page, 6, Sort.by("createdAt").descending());

        LikeWellnessInfoVo vo = mock(LikeWellnessInfoVo.class);
        when(vo.getPlaceId()).thenReturn("place-id-123");
        when(vo.getThema()).thenReturn(Thema.STAY);
        Page<LikeWellnessInfoVo> likePage = new PageImpl<>(List.of(vo), pageable, 1);


        PlaceReviewResponse.PlaceResult placeResult = mock(PlaceReviewResponse.PlaceResult.class);
        PlaceReviewResponse response = mock(PlaceReviewResponse.class);
        when(response.getResult()).thenReturn(placeResult);
        when(googleMapInfoService.getPlaceDetails("place-id-123")).thenReturn(Mono.just(response));
        when(likedRepository.findLikedThemaByMember(mockMember))
                .thenReturn(List.of(thema));
        when(likedRepository.findByMemberIdAndThemaIn(eq(mockMember), eq(List.of(thema)), any(Pageable.class)))
                .thenReturn(likePage);

        // when
        LikedResponse.LikedList result = likedService.LikedList(thema, page);

        // then
        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getPageNum());
        assertFalse(result.isHasPrevious());
        assertFalse(result.isHasNext());
        assertEquals(1, result.getWellnessInfoList().size());
        assertEquals(List.of(thema), result.getThemaList());
    }
}
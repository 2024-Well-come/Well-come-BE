package com.wellcome.WellcomeBE.domain.like.service;

import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.MEMBER_NOT_FOUND;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.WELLNESSINFO_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LikedService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final LikedRepository likedRepository;
    private final WellnessInfoRepository wellnessInfoRepository;

    /**
     * 좋아요 등록
     */
    public void createLiked(Long wellnessInfoId) {

        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(WELLNESSINFO_NOT_FOUND));

        Member member = memberRepository.findById(tokenProvider.getMemberId())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        // 좋아요 내역이 없을 경우, 좋아요 등록
        likedRepository.findByMemberId(member.getId())
                .orElseGet(() -> likedRepository.save(Liked.create(wellnessInfo, member)));
    }

}

package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostDetailResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoNearResponse;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal.WellnessInfoWithLiked;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WellnessInfoRepositoryCustom {
    List<WellnessInfoNearResponse.WellnessNearbyDto> findNearbyWithinRadius(Double mapX, Double mapY, Long excludeId, Double radiusKm, int limit);

    List<ReviewPostDetailResponse.ReviewWellnessInfoItem> findWellnessInfoWithSupportAndLikedByCommunityId(Long communityId, Member member);

    Page<WellnessInfoWithLiked> findWellnessInfoByThemaAndSigunguWithWeight(
            Pageable pageable,
            @Param("member") Member member,
            @Param("themaList") List<Thema> themaList,
            @Param("sigunguList") List<Sigungu> sigunguList,
            @Param("thema1") Thema thema1,
            @Param("thema2") Thema thema2,
            @Param("thema3") Thema thema3
    );

    List<Thema> findDistinctThema(@Param("themaList") List<Thema> themaList);

    List<Sigungu> findDistinctSigungu(@Param("sigunguList") List<Sigungu> sigunguList);

    List<String> findContentIdsWithoutDetailImg();
}

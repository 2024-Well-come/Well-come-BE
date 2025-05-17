package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostDetailResponse;
import com.wellcome.WellcomeBE.domain.member.Member;

import java.util.List;

public interface WellnessInfoRepositoryCustom {
    public List<ReviewPostDetailResponse.ReviewWellnessInfoItem> findWellnessInfoWithSupportAndLikedByCommunityId(Long communityId, Member member);
}

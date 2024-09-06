package com.wellcome.WellcomeBE.domain.like.repository;

import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikedRepository extends JpaRepository<Liked,Long> {
    Boolean findLikedByWellnessInfoAndMember(WellnessInfo wellnessInfo, Member member);

    Optional<Liked> findByMemberId(Long memberId);
}

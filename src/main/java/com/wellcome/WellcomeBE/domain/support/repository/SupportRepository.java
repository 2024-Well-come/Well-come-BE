package com.wellcome.WellcomeBE.domain.support.repository;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.Support;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRepository extends JpaRepository<Support,Long> {
    boolean existsByCommunityAndMember(Community community, Member member);
}

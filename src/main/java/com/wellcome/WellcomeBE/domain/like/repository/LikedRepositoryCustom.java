package com.wellcome.WellcomeBE.domain.like.repository;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.global.type.Thema;

import java.util.List;

public interface LikedRepositoryCustom {
    List<Thema> findThemaByMemberOrderByLikedCount(Member member);

}
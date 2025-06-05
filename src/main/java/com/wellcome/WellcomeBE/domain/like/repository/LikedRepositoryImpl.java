package com.wellcome.WellcomeBE.domain.like.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.global.type.Thema;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.wellcome.WellcomeBE.domain.like.QLiked.liked;
import static com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo.wellnessInfo;

public class LikedRepositoryImpl implements LikedRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    public LikedRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Thema> findThemaByMemberOrderByLikedCount(Member member) {

        return queryFactory
                .select(wellnessInfo.thema)
                .from(liked)
                .join(wellnessInfo).on(liked.wellnessInfo.eq(wellnessInfo))
                .where(liked.member.eq(member))
                .groupBy(wellnessInfo.thema)
                .orderBy(liked.id.count().desc())
                .fetch();
    }

}
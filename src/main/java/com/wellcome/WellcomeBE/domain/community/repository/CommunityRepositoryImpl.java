package com.wellcome.WellcomeBE.domain.community.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.community.Community;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.wellcome.WellcomeBE.domain.community.QCommunity.community;
import static com.wellcome.WellcomeBE.domain.support.QSupport.support;

public class CommunityRepositoryImpl implements CommunityRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    public CommunityRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 커뮤니티 후기 게시글 목록 조회 (추천순 정렬)
    @Override
    public Page<Community> findByPostTypeOrderBySupportCount(Pageable pageable, Community.PostType postType) {

        JPAQuery<Community> query = queryFactory
                .select(community)
                .from(community)
                .leftJoin(support).on(community.id.eq(support.community.id))
                .where(community.postType.eq(postType))
                .groupBy(community.id)
                .orderBy(
                        support.community.id.count().desc(),
                        community.createdAt.desc()
                );

        List<Community> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(community.countDistinct())
                .from(community)
                .where(community.postType.eq(postType))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}


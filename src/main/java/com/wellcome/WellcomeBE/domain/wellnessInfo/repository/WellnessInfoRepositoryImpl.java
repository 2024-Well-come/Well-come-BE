package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.like.QLiked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal.WellnessInfoWithLiked;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.wellcome.WellcomeBE.domain.like.QLiked.liked;
import static com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo.wellnessInfo;

public class WellnessInfoRepositoryImpl implements WellnessInfoRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QWellnessInfo w =wellnessInfo;
    private final QLiked l =liked;

    public WellnessInfoRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     *웰니스 장소 목록 조회
     */
    @Override
    public Page<WellnessInfoWithLiked> findWellnessInfoByThemaAndSigunguWithWeight(Pageable pageable, Member member, List<Thema> themaList, List<Sigungu> sigunguList, Thema thema1, Thema thema2, Thema thema3) {

        // 좋아요를 많이 누른 테마, 조회수 가중치를 반영한 정렬 조건 생성
        NumberExpression<Integer> themaWeight = new CaseBuilder()
                .when(themaEq(thema1)).then(30)
                .when(themaEq(thema2)).then(20)
                .when(themaEq(thema3)).then(10)
                .otherwise(0);

        NumberTemplate<Double> score = Expressions.numberTemplate(
                Double.class,
                "({0} * 0.3 + {1} * 0.7)",
                themaWeight, w.view
        );

        // 페이지네이션 대상 데이터 조회
        List<WellnessInfoWithLiked> content = queryFactory
                .select(Projections.constructor(WellnessInfoWithLiked.class,
                        w,
                        new CaseBuilder()
                                .when(l.id.isNotNull()).then(true)
                                .otherwise(false)
                ))
                .from(w)
                .leftJoin(l).on(liked.wellnessInfo.eq(w)
                        .and(l.member.eq(member)))
                .where(
                        themaIn(themaList),
                        sigunguIn(sigunguList)
                )
                .orderBy(score.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 데이터 개수 조회
        JPAQuery<Long> countQuery = queryFactory
                .select(w.count())
                .from(w)
                .leftJoin(l).on(liked.wellnessInfo.eq(w)
                        .and(l.member.eq(member)))
                .where(
                        themaIn(themaList),
                        sigunguIn(sigunguList)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     *테마,지역 조회
     */
    @Override
    public List<Thema> findDistinctThema(List<Thema> themaList) {
        return queryFactory
                .select(w.thema).distinct()
                .from(w)
                .where(themaIn(themaList))
                .fetch();
    }

    @Override
    public List<Sigungu> findDistinctSigungu(List<Sigungu> sigunguList) {
        return queryFactory
                .select(w.sigungu).distinct()
                .from(w)
                .where(sigunguIn(sigunguList))
                .fetch();
    }

    private BooleanExpression themaEq(Thema thema) {
        return thema != null ? w.thema.eq(thema) : Expressions.FALSE;
    }

    private BooleanExpression themaIn(List<Thema> themaList) {
        return CollectionUtils.isEmpty(themaList) ? null : w.thema.in(themaList);
    }

    private BooleanExpression sigunguIn(List<Sigungu> sigunguList) {
        return CollectionUtils.isEmpty(sigunguList) ? null : w.sigungu.in(sigunguList);
    }

}

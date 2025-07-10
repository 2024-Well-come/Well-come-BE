package com.wellcome.WellcomeBE.domain.tripPlanPlace.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.QTripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.type.Thema;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.wellcome.WellcomeBE.domain.tripPlanPlace.QTripPlanPlace.tripPlanPlace;
import static com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo.wellnessInfo;


public class TripPlanPlaceRepositoryImpl implements TripPlanPlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QTripPlanPlace tpp =tripPlanPlace;
    private final QWellnessInfo w =wellnessInfo;

    public TripPlanPlaceRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<TripPlanPlace> findByTripPlanIdAndThema(PageRequest pageRequest, Long planId, Thema thema) {

        List<TripPlanPlace> content = queryFactory
                .selectFrom(tpp)
                .join(tpp.wellnessInfo, w).fetchJoin()
                .where(
                        tpp.tripPlan.id.eq(planId),
                        tpp.status.eq(TripPlan.Status.ACTIVE),
                        themaEq(thema)
                )
                .orderBy(tpp.createdAt.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = queryFactory
                .select(tpp.id.count())
                .from(tpp)
                .join(tpp.wellnessInfo, w)
                .where(
                        tpp.tripPlan.id.eq(planId),
                        tpp.status.eq(TripPlan.Status.ACTIVE),
                        themaEq(thema)
                )
                .fetchOne();

        return new PageImpl<>(content, pageRequest, total);
    }

    @Override
    public List<TripPlan> findByWellnessInfoAndReviewConditionsExist(WellnessInfo wellnessInfo) {

        return queryFactory
                .select(tpp.tripPlan)
                .from(tpp)
                .where(
                        tpp.wellnessInfo.eq(wellnessInfo),
                        tpp.rating.isNotNull().or(tpp.review.isNotNull())
                )
                .fetch();
    }

    private BooleanExpression themaEq(Thema thema) {
        return thema != null ? w.thema.eq(thema) : null;
    }
}


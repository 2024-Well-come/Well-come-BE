package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.community.QCommunity;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostDetailResponse;
import com.wellcome.WellcomeBE.domain.like.QLiked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.support.QSupport;
import com.wellcome.WellcomeBE.domain.tripPlan.QTripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.QTripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo;
import jakarta.persistence.EntityManager;
import com.querydsl.core.types.Projections;
import java.util.List;

import static com.wellcome.WellcomeBE.domain.like.QLiked.liked;
import static com.wellcome.WellcomeBE.domain.support.QSupport.support;
import static com.wellcome.WellcomeBE.domain.tripPlanPlace.QTripPlanPlace.tripPlanPlace;
import static com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo.wellnessInfo;

public class WellnessInfoRepositoryImpl implements WellnessInfoRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    public WellnessInfoRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ReviewPostDetailResponse.ReviewWellnessInfoItem> findWellnessInfoWithSupportAndLikedByCommunityId(Long communityId, Member member) {
        QWellnessInfo w = wellnessInfo;
        QTripPlanPlace tpp = tripPlanPlace;
        QTripPlan tp = QTripPlan.tripPlan;
        QCommunity c = QCommunity.community;
        QLiked l = liked;
        QSupport s = support;

        return queryFactory
                .select(Projections.constructor(ReviewPostDetailResponse.ReviewWellnessInfoItem.class,
                        wellnessInfo.id,
                        wellnessInfo.thumbnailUrl,
                        wellnessInfo.title,
                        wellnessInfo.thema.stringValue(),
                        wellnessInfo.address,
                        tripPlanPlace.review,
                        tripPlanPlace.rating,
                        support.id.isNotNull(),
                        liked.id.isNotNull()
                ))
                .from(w)
                .leftJoin(tpp).on(tpp.wellnessInfo.eq(w))
                .leftJoin(tp).on(tp.id.eq(tpp.tripPlan.id))
                .leftJoin(c).on(c.tripPlan.id.eq(tp.id))
                .leftJoin(l).on(l.wellnessInfo.eq(w).and(l.member.eq(member)))
                .leftJoin(s).on(s.wellnessInfo.eq(w).and(s.member.eq(member)))
                .where(
                        c.id.eq(communityId),
                        tpp.review.isNotNull().or(tpp.rating.isNotNull())
                )
                .fetch();
    }
}


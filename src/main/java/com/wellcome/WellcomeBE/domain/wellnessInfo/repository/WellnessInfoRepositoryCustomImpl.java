package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoNearResponse;
import jakarta.persistence.EntityManager;

import java.util.List;

public class WellnessInfoRepositoryCustomImpl implements WellnessInfoRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final NativeSqlCreator nativeSqlCreator;
    public WellnessInfoRepositoryCustomImpl(EntityManager em, NativeSqlCreator nativeSqlCreator) {
        this.queryFactory = new JPAQueryFactory(em);
        this.nativeSqlCreator = nativeSqlCreator;
    }

    @Override
    public List<WellnessInfoNearResponse.WellnessNearbyDto> findNearbyWithinRadius(
            Double mapX, Double mapY, Long excludeId, Double radiusKm, long limit
    ) {
        QWellnessInfo wi = QWellnessInfo.wellnessInfo;

        NumberExpression<Double> distanceExpr = nativeSqlCreator.createCalcDistanceSQL(
                mapX, mapY, wi.mapX, wi.mapY
        );

        return queryFactory
                .select(Projections.constructor(WellnessInfoNearResponse.WellnessNearbyDto.class,
                        wi.id,
                        wi.thumbnailUrl,
                        wi.title,
                        wi.category,
                        wi.mapX,
                        wi.mapY,
                        wi.thema,
                        wi.parentId,
                        distanceExpr.as("distance")
                ))
                .from(wi)
                .where(
                        wi.id.ne(excludeId),
                        distanceExpr.loe(radiusKm * 1000)
                )
                .orderBy(distanceExpr.asc())
                .limit(limit)
                .fetch();
    }

}

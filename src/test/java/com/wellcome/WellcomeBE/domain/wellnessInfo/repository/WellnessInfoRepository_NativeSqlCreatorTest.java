package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.wellnessInfo.QWellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoNearResponse;
import com.wellcome.WellcomeBE.global.type.Category;
import com.wellcome.WellcomeBE.global.type.Thema;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WellnessInfoRepository_NativeSqlCreatorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private NativeSqlCreator nativeSqlCreator;

    @Mock
    private JPAQuery<WellnessInfoNearResponse.WellnessNearbyDto> query;

    private JPAQueryFactory queryFactory;
    private WellnessInfoRepositoryImpl wellnessInfoRepository;

    @BeforeEach
    void setUp() {
        queryFactory = mock(JPAQueryFactory.class);
        wellnessInfoRepository = new WellnessInfoRepositoryImpl(queryFactory, nativeSqlCreator);
    }

    // 단위 테스트
    @Test
    void findNearbyWithinRadius() {
        // given
        double mapX = 126.9780;
        double mapY = 37.5665;
        long excludeId = 1L;
        double radiusKm = 5.0;
        int limit = 6;

        QWellnessInfo wi = QWellnessInfo.wellnessInfo;
        NumberExpression<Double> distanceExpr = Expressions.numberTemplate(Double.class, "ST_Distance_Sphere(...)");

        when(nativeSqlCreator.createCalcDistanceSQL(mapX, mapY, wi.mapX, wi.mapY)).thenReturn(distanceExpr);

        List<WellnessInfoNearResponse.WellnessNearbyDto> mockResult = List.of(
                new WellnessInfoNearResponse.WellnessNearbyDto(
                        2L,
                        "Healing Spa",
                        "1",
                        Category.ACCOMMODATION,
                        126.98,
                        37.56,
                        Thema.NATURE,
                        "http://example.com/thumb.jpg",
                        300.0
                )
        );

        JPAQuery<WellnessInfoNearResponse.WellnessNearbyDto> mockQuery = mock(JPAQuery.class);
        when(queryFactory.select((Expression<WellnessInfoNearResponse.WellnessNearbyDto>) any()))
                .thenReturn(mockQuery);
        when(mockQuery.from(wi)).thenReturn(mockQuery);
        when(mockQuery.where(any(Predicate.class), any(Predicate.class))).thenReturn(mockQuery);
        when(mockQuery.orderBy(any(OrderSpecifier.class))).thenReturn(mockQuery);
        when(mockQuery.limit(limit)).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(mockResult);

        // when
        List<WellnessInfoNearResponse.WellnessNearbyDto> result =
                wellnessInfoRepository.findNearbyWithinRadius(mapX, mapY, excludeId, radiusKm, limit);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Healing Spa");
        assertThat(result.get(0).getDistance()).isEqualTo(300.0);
    }

}

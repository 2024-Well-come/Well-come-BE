package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.stereotype.Component;

@Component
public class MysqlNativeSqlCreator implements NativeSqlCreator {
    @Override
    public NumberExpression<Double> createCalcDistanceSQL(Double longitudeCond, Double latitudeCond,
                                                          NumberPath<Double> dbLongitude, NumberPath<Double> dbLatitude) {
        return Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere(POINT({0}, {1}), POINT({2}, {3}))",
                longitudeCond, latitudeCond, dbLongitude, dbLatitude);
    }
}

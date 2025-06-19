package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.stereotype.Component;

@Component
public class MysqlNativeSqlCreator {
    public NumberExpression<Double> createCalcDistanceSQL(Double longitudeCond, Double latitudeCond,
                                                          NumberPath<Double> dbLongitude, NumberPath<Double> dbLatitude) {
        return Expressions.numberTemplate(Double.class, "ST_Distance_Sphere({0}, {1})",
                Expressions.stringTemplate("POINT({0}, {1})", longitudeCond, latitudeCond),
                Expressions.stringTemplate("POINT({0}, {1})", dbLongitude, dbLatitude)
        );
    }
}

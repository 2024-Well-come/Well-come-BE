package com.wellcome.WellcomeBE.domain.Article.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.Article.QArticle;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    public ArticleRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Article> findRandomArticles() {
        return queryFactory
                .selectFrom(QArticle.article)
                .orderBy(Expressions.numberTemplate(Double.class, "rand()").asc())
                .limit(5)
                .fetch();
    }
}

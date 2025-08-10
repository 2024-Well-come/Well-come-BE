package com.wellcome.WellcomeBE.domain.Article.repository;

import com.wellcome.WellcomeBE.domain.Article.Article;

import java.util.List;

public interface ArticleRepositoryCustom {
    List<Article> findRandomArticles();
}

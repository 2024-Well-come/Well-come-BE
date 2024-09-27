package com.wellcome.WellcomeBE.domain.Article.service;

import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.Article.dto.ArticleResponse;
import com.wellcome.WellcomeBE.domain.Article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private static final int ARTICLE_LIST_SIZE = 5;
    private static final int ARTICLE_PAGE_SIZE = 5;

    public ArticleResponse.ArticleBest getPopularArticles() {
        // 최근 한 달 전의 날짜를 LocalDateTime으로 가져옵니다.
        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);

        // 최근 한 달 동안 조회수가 높은 아티클 가져오기
        List<Article> topArticles = articleRepository.findTopArticles(oneMonthAgo, PageRequest.of(0, ARTICLE_LIST_SIZE));

        // 만약 조회수가 모두 5 미만이라면 랜덤으로 아티클 가져오기
        if (topArticles.isEmpty()) {
            topArticles = articleRepository.findRandomArticles(); // 랜덤으로 아티클 가져오기
        }

        return ArticleResponse.ArticleBest.from(topArticles.stream()
                .map(ArticleResponse.ArticleItem::from)
                .collect(Collectors.toList()));

    }

    public ArticleResponse.ArticleBrief getArticles(int page) {
        Pageable pageable = PageRequest.of(page, ARTICLE_PAGE_SIZE);
        Page<Article> articles = articleRepository.findAllArticleByCreatedAt(pageable);
        List<ArticleResponse.ArticleItem> items = articles.stream()
                .map(ArticleResponse.ArticleItem::from)
                .collect(Collectors.toList());
        return ArticleResponse.ArticleBrief.from(items,articles.getTotalElements(),articles.getTotalPages(),articles.hasPrevious(),articles.hasNext());
    }

}


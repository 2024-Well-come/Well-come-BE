package com.wellcome.WellcomeBE.domain.Article.service;

import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.Article.dto.ArticleResponse;
import com.wellcome.WellcomeBE.domain.Article.repository.ArticleRepository;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.ARTICLE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final GoogleMapInfoService googleMapInfoService;
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

    public ArticleResponse.ArticleDetail getArticleDetail(Long articleId) {
        // 1. ID로 아티클을 가져옵니다.
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ARTICLE_NOT_FOUND));

        WellnessInfo wellnessInfo = article.getWellnessInfo();
        // 3. IncludeWellnessInfoItem DTO로 변환
        PlaceReviewResponse.PlaceResult placeResult = (wellnessInfo.getParentId() == null)
                ? null
                : googleMapInfoService.getPlaceDetails(wellnessInfo.getParentId()).block().getResult(); // 안전하게 장소 정보를 가져옵니다.

        ArticleResponse.IncludeWellnessInfoItem wellnessInfoItem = ArticleResponse.IncludeWellnessInfoItem.from(wellnessInfo, placeResult);

        // 4. ArticleDetail DTO로 변환하여 반환
        return ArticleResponse.ArticleDetail.from(article, Collections.singletonList(wellnessInfoItem)); // 리스트로 변환하여 반환
    }

}


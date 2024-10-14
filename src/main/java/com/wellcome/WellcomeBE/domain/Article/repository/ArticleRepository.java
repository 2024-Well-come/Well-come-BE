package com.wellcome.WellcomeBE.domain.Article.repository;

import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 최근 한 달 동안 조회수가 높은 아티클을 가져오는 쿼리
    @Query("SELECT a FROM Article a WHERE a.createdAt >= :startDate ORDER BY a.view DESC")
    List<Article> findTopArticles(LocalDateTime startDate, Pageable pageable);

    // 랜덤으로 5개 아티클 가져오는 쿼리
    @Query(value = "SELECT * FROM Article ORDER BY RANDOM() LIMIT 5", nativeQuery = true)
    List<Article> findRandomArticles();

    @Query("SELECT a FROM Article a ORDER BY a.createdAt DESC")
    Page<Article> findAllArticleByCreatedAt(Pageable pageable);

    // 최신 5개의 웰니스 정보를 담은 아티클을 조회
    List<Article> findTop5ByWellnessInfoOrderByCreatedAt(@Param("wellnessInfo") WellnessInfo wellnessInfo);

}

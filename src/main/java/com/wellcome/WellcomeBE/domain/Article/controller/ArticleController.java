package com.wellcome.WellcomeBE.domain.Article.controller;

import com.wellcome.WellcomeBE.domain.Article.dto.ArticleResponse;
import com.wellcome.WellcomeBE.domain.Article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/best")
    public ResponseEntity<ArticleResponse.ArticleBest> getPopularArticles() {
        return ResponseEntity.ok(articleService.getPopularArticles());
    }

    @GetMapping("/brief")
    public ResponseEntity<ArticleResponse.ArticleBrief> getArticles(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(articleService.getArticles(page));
    }

    @GetMapping("{articleId}")
    public ResponseEntity<ArticleResponse.ArticleDetail> getArticleDetails(@PathVariable Long articleId){
        return ResponseEntity.ok(articleService.getArticleDetail(articleId));
    }
}
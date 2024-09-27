package com.wellcome.WellcomeBE.domain.Article.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wellcome.WellcomeBE.domain.Article.Article;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArticleResponse {

    @Getter
    @Builder
    public static class ArticleBrief {
        List<ArticleItem> articleItem;

        public static ArticleBrief from(List<ArticleResponse.ArticleItem> items) {
            return ArticleBrief.builder().articleItem(items).build();
        }
    }


    @Getter
    @Builder
    public static class ArticleItem{
        private Long id;

        private String title;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String subtitle;

        private String thumbnailUrl;

        public static ArticleItem from(Article article) {
            return ArticleItem.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .subtitle(article.getSubtitle())
                    .thumbnailUrl(article.getThumbnailUrl())
                    .build();
        }
    }

}

package com.wellcome.WellcomeBE.domain.Article.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArticleResponse {

    @Getter
    @Builder
    public static class ArticleBest {
        List<ArticleItem> articleItem;

        public static ArticleBest from(List<ArticleResponse.ArticleItem> items) {
            return ArticleBest.builder().articleItem(items).build();
        }
    }


    @Getter
    @Builder
    public static class ArticleBrief {
        private long totalCount;
        private int pageNum;
        private boolean hasPrevious;
        private boolean hasNext;
        private List<ArticleItem> data;

        public static ArticleBrief from(List<ArticleResponse.ArticleItem> items,long totalCount,int pageNum,boolean hasPrevious,boolean hasNext) {
            return ArticleBrief.builder()
                    .data(items)
                    .totalCount(totalCount)
                    .pageNum(pageNum)
                    .hasPrevious(hasPrevious)
                    .hasNext(hasNext)
                    .build();
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

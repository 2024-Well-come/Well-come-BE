package com.wellcome.WellcomeBE.domain.Article.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.OpeningHoursUtils;
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
        private Long articleId;

        private String title;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String subtitle;

        private String thumbnailUrl;

        public static ArticleItem from(Article article) {
            return ArticleItem.builder()
                    .articleId(article.getId())
                    .title(article.getTitle())
                    .subtitle(article.getSubtitle())
                    .thumbnailUrl(article.getThumbnailUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ArticleDetail {
        // 관리자 고정값 상수 정의
        private static final String DEFAULT_NICKNAME = "웰컴";
        private static final String DEFAULT_IMAGE_URL = "https://default-image-url.com/admin.png";

        private Long articleId;
        private String nickname;
        private String imageUrl;
        private String createdAt;
        private String title;
        private String thumbnailUrl;
        private String content;
        private List<IncludeWellnessInfoItem> items;

        public static ArticleDetail from(Article article, List<IncludeWellnessInfoItem> items) {
            return ArticleDetail.builder()
                    .articleId(article.getId())
                    .nickname(DEFAULT_NICKNAME) // 고정 닉네임 사용
                    .imageUrl(DEFAULT_IMAGE_URL) // 고정 이미지 URL 사용
                    .createdAt(article.getCreatedAt().toString())
                    .title(article.getTitle())
                    .thumbnailUrl(article.getThumbnailUrl())
                    .content(article.getContent())
                    .items(items)
                    .build();
        }
    }


        @Getter
        @Builder
        public static class IncludeWellnessInfoItem{
            private String title;
            private String category;
            private double rating;
            private Boolean isOpen;
            private String thema;
            public static IncludeWellnessInfoItem from(WellnessInfo wellnessInfo , PlaceReviewResponse.PlaceResult placeResult) {
                return IncludeWellnessInfoItem.builder()
                        .title(wellnessInfo.getTitle())
                        .category(wellnessInfo.getCategory().getName())
                        .rating(placeResult.getRating()) // use corrected field name
                        .isOpen(OpeningHoursUtils.getOpenStatus(placeResult).getIsOpen())
                        .thema(wellnessInfo.getThema().getName())
                        .build();

        }

    }

}

package com.wellcome.WellcomeBE.domain.Article.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wellcome.WellcomeBE.domain.Article.Article;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
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
                    .subtitle(generateSubtitle(article.getSubtitle(), article.getContent()))
                    .thumbnailUrl(article.getThumbnailUrl())
                    .build();
        }

        /**
         * subtitle이 없는 경우 content에서 일정 글자 수를 잘라 subtitle로 생성합니다.
         */
        private static String generateSubtitle(String subtitle, String content) {
            if (subtitle != null && !subtitle.isEmpty()) {
                return subtitle;
            }

            // content가 존재할 경우 최대 20자까지 추출하고, 길이를 초과하면 "..." 추가
            if (content != null && !content.isEmpty()) {
                int maxLength = 20;
                return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
            }

            return null; // subtitle도 content도 없을 경우 null 반환
        }
    }

    @Getter
    @Builder
    public static class ArticleDetail {
        private Long articleId;
        private String createdAt;
        private String title;
        private String thumbnailUrl;
        private String content;
        private List<IncludeWellnessInfoItem> items;

        public static ArticleDetail from(Article article, List<IncludeWellnessInfoItem> items) {
            return ArticleDetail.builder()
                    .articleId(article.getId())
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
            private Long wellnessInfoId;
            private String title;
            private String category;
            private double rating;
            private int reviewNum;
            private String thema;
            public static IncludeWellnessInfoItem from(WellnessInfo wellnessInfo , PlaceReviewResponse.PlaceResult placeResult) {
                return IncludeWellnessInfoItem.builder()
                        .wellnessInfoId(wellnessInfo.getId())
                        .title(wellnessInfo.getTitle())
                        .category(wellnessInfo.getCategory().getName())
                        .rating(placeResult.getRating())
                        .reviewNum(placeResult != null ? placeResult.getUser_ratings_total() : 0)
                        .thema(wellnessInfo.getThema().getName())
                        .build();

        }

    }

}

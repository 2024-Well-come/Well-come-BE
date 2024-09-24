package com.wellcome.WellcomeBE.domain.community.dto.response;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.communityImg.CommunityImg;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 후기 게시글 목록 조회 Response
 */
@Getter
@Builder
public class ReviewPostResponse {

    private List<ReviewPostBrief> recommendReviewList;
    private ReviewPostList reviewList;

    public static ReviewPostResponse from (
            List<ReviewPostBrief> recommendReviewList,
            ReviewPostList reviewList
    ){
        return ReviewPostResponse.builder()
                .recommendReviewList(recommendReviewList)
                .reviewList(reviewList)
                .build();
    }

    @Getter
    @Builder
    public static class ReviewPostList{
        private long totalCount;
        private int pageNum;
        private boolean hasPrevious;
        private boolean hasNext;
        private List<ReviewPostBrief> data;

        public static ReviewPostList from (
                long totalCount,
                int pageNum,
                boolean hasPrevious,
                boolean hasNext,
                List<ReviewPostBrief> data
        ){
            return ReviewPostList.builder()
                    .totalCount(totalCount)
                    .pageNum(pageNum)
                    .hasPrevious(hasPrevious)
                    .hasNext(hasNext)
                    .data(data)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ReviewPostBrief {
        private Long communityId;
        private String thumbnailUrl;
        private String title;
        private String content;
        private String postDate;

        public static ReviewPostBrief from (Community community){
            // 게시글 작성 날짜 (createdAt 기준)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy년 MM월 dd일");
            String postDate = community.getCreatedAt() != null ? community.getCreatedAt().format(formatter) : null;

            // 썸네일 이미지
            String thumbnailUrl = null;
            List<CommunityImg> imgList = community.getCommunityImgs();
            if(imgList != null && !imgList.isEmpty()){
                thumbnailUrl = imgList.get(0).getImgUrl();
            }

            return ReviewPostBrief.builder()
                    .communityId(community.getId())
                    .thumbnailUrl(thumbnailUrl)
                    .title(community.getTitle())
                    .content(community.getContent())
                    .postDate(postDate)
                    .build();
        }
    }
}

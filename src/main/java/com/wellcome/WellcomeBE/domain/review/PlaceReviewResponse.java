package com.wellcome.WellcomeBE.domain.review;

import lombok.Data;

import java.util.List;

@Data
public class PlaceReviewResponse {
    private List<Object> html_attributions;  // HTML 속성
    private PlaceResult result;              // 장소 결과
    private String status;                   // 상태

    @Data
    public static class PlaceResult {
        private String formatted_phone_number;   // 전화번호
        private String name;                     // 장소 이름
        private OpeningHours opening_hours;      // 운영 시간
        private double rating;                   // 평점
        private List<PlaceReview> reviews;       // 리뷰 리스트

        @Data
        public static class OpeningHours {
            private boolean open_now;              // 현재 열려 있는지 여부
            private List<Period> periods;          // 영업시간 기간 리스트
            private List<String> weekday_text;     // 요일별 텍스트 표현

            @Data
            public static class Period {
                private OpenCloseTime open;   // 오픈 시간
                private OpenCloseTime close;  // 마감 시간

                @Data
                public static class OpenCloseTime {
                    private int day;      // 요일 (0=일요일, 1=월요일, ...)
                    private String time;  // 시간 (HHMM 형식)
                }
            }
        }

        @Data
        public static class PlaceReview {
            private String author_name;            // 작성자 이름
            private String author_url;             // 작성자 URL
            private String language;               // 언어 코드
            private String original_language;      // 원본 언어
            private String profile_photo_url;      // 프로필 사진 URL
            private int rating;                    // 리뷰 평점
            private String relative_time_description; // 상대적 시간 설명 (ex. "1달 전")
            private String text;                   // 리뷰 내용
            private long time;                     // 리뷰 시간 (타임스탬프)
            private boolean translated;            // 번역 여부
        }
    }
}

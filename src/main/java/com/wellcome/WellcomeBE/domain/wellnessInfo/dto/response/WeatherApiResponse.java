package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
public class WeatherApiResponse {

    private Response response;

    @Data
    public static class Response {

        private Header header;
        private Body body;

        @Data
        public static class Header {
            private String resultCode;
            private String resultMsg;
        }

        @Data
        public static class Body {
            private String dataType;
            private Items items;
            private int numOfRows;
            private int pageNo;
            private int totalCount;

            @Data
            public static class Items {
                private List<Item> item;

                @Data
                public static class Item {
                    private String baseDate;
                    private String baseTime;
                    private String nx;
                    private String ny;
                    private String category;
                    private String fcstDate;
                    private String fcstTime;
                    private String fcstValue;
                }
            }
        }
    }
}

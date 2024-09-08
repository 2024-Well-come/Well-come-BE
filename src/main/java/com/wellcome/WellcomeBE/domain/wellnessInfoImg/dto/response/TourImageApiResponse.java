package com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourImageApiResponse {
    @JsonProperty("response")
    private Response response;

    @Data
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;

        @Data
        public static class Header {
            @JsonProperty("resultCode")
            private String resultCode;

            @JsonProperty("resultMsg")
            private String resultMsg;
        }

        @Data
        public static class Body {
            @JsonDeserialize(using = ItemsDeserializer.class)
            private Items items;

            private int numOfRows;
            private int pageNo;
            private int totalCount;

            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Items {
                private List<Item> item;

                @Data
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class Item {
                    private String contentid;
                    private String originimgurl;
                    private String imgname;
                    private String smallimageurl;
                    private String cpyrhtDivCd;
                    private String serialnum;
                }
            }
        }
    }
}
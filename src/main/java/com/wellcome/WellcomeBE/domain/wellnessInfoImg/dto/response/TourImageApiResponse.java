package com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.response;

import lombok.Data;
import lombok.Getter;


import java.util.List;

@Getter
public class TourImageApiResponse {
    private Header header;
    private Body body;

    @Getter
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    public static class Body {
        private Items items;

        @Getter
        public static class Items {
            private List<Item> item;
        }

        @Data
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

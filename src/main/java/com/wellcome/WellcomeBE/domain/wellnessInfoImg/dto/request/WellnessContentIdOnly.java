package com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request;

public class WellnessContentIdOnly {

    private final String contentId;

        public WellnessContentIdOnly(String contentId) {
            this.contentId = contentId;
        }

        public String getContentId() {
            return contentId;
        }
}

package com.wellcome.WellcomeBE.domain.review;

import lombok.Data;

import java.util.List;

@Data
public class PlacePredictionResponse {

    private List<Prediction> predictions;
    private String status;

    @Data
    public static class Prediction {
        private String description;
        private List<MatchedSubstring> matchedSubstrings;
        private String placeId;
        private String reference;
        private StructuredFormatting structuredFormatting;
        private List<Term> terms;
        private List<String> types;


        @Data
        public static class MatchedSubstring {
            private int length;
            private int offset;

        }

        @Data
        public static class StructuredFormatting {
            private String mainText;
            private List<MainTextMatchedSubstring> mainTextMatchedSubstrings;
            private String secondaryText;


            @Data
            public static class MainTextMatchedSubstring {
                private int length;
                private int offset;

            }
        }

        @Data
        public static class Term {
            private int offset;
            private String value;

        }
    }
}

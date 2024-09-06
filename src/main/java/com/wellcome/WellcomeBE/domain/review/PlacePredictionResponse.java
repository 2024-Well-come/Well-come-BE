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
        private List<MatchedSubstring> matched_substrings;
        private String place_id;
        private String reference;
        private StructuredFormatting structured_formatting;
        private List<Term> terms;
        private List<String> types;

        @Data
        public static class MatchedSubstring {
            private int length;
            private int offset;

        }

        @Data
        public static class StructuredFormatting {
            private String main_text;
            private List<MatchedSubstring> main_text_matched_substrings;
            private String secondary_text;
            private List<MatchedSubstring> secondary_text_matched_substrings;

        }

        @Data
        public static class Term {
            private int offset;
            private String value;

        }
    }
}

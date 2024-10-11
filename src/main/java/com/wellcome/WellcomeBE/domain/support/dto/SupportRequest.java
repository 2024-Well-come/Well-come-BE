package com.wellcome.WellcomeBE.domain.support.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class SupportRequest {

    @Getter
    @Builder
    public static class CreateCommunityInWellnessSupportRequestDto {
        private static final String message = "추가할 추천 정보가 없습니다.";

        @NotNull
        private Long communityId;

        @NotNull(message = message)
        private Long wellnessInfoId;  // wellnessInfo ID 리스트
    }



    @Getter
    @Builder
    public static class DeleteCommunityInWellnessSupportRequestDto {
        @NotNull
        private Long communityId;   // 삭제할 커뮤니티 ID
        @NotNull
        private Long wellnessInfoId; // 삭제할 WellnessInfo ID
    }

}

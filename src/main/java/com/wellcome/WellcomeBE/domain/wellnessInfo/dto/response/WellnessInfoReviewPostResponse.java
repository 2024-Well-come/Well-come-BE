package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.wellcome.WellcomeBE.domain.community.dto.request.ReviewPostRequest;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WellnessInfoReviewPostResponse {

    private List<ReviewPostResponse.ReviewPostBrief> reviewList;

}

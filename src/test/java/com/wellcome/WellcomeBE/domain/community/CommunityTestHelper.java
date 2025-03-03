package com.wellcome.WellcomeBE.domain.community;

import com.wellcome.WellcomeBE.domain.community.dto.request.ReviewPostRequest;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class CommunityTestHelper {

    public static TripPlan createTestTripPlan(Member member){
        TripPlan tripPlan = TripPlan.builder()
                .title("title")
                .startDate(LocalDate.parse("2025-01-01"))
                .endDate(LocalDate.parse("2025-01-05"))
                .member(member)
                .isActive(true)
                .status(TripPlan.Status.ACTIVE)
                .build();
        ReflectionTestUtils.setField(tripPlan, "id", 100L);
        return tripPlan;
    }

    public static TripPlanPlace createTestTripPlanPlace(WellnessInfo wellnessInfo, TripPlan tripPlan){
        return TripPlanPlace.builder()
                .tripPlan(tripPlan)
                .wellnessInfo(wellnessInfo)
                .build();
    }

    public static ReviewPostRequest createReviewPostRequest(long tripPlanId, boolean hasReviewList){
        ReviewPostRequest request = new ReviewPostRequest();
        ReflectionTestUtils.setField(request, "planId", tripPlanId);
        ReflectionTestUtils.setField(request, "title", "title");
        ReflectionTestUtils.setField(request, "content", "content");

        if(hasReviewList){
            List<ReviewPostRequest.UserReview> reviewList = new ArrayList<>();
            for(int i = 0; i < 3; i++){
                WellnessInfo wellnessInfo = Mockito.mock(WellnessInfo.class);
                when(wellnessInfo.getId()).thenReturn(1000L * (i+1));
                reviewList.add(new ReviewPostRequest.UserReview(wellnessInfo.getId(), i+1, "review"+(i+1)));
            }
            ReflectionTestUtils.setField(request, "reviewList", reviewList);
        }
        return request;
    }

}

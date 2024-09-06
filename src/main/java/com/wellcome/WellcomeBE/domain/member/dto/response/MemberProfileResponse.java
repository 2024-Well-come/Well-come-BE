package com.wellcome.WellcomeBE.domain.member.dto.response;

import com.wellcome.WellcomeBE.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileResponse {

    private Long memberId;
    private String nickname;
    private String profileImgUrl;

    public static MemberProfileResponse from(Member member){
        return MemberProfileResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImg())
                .build();
    }

}

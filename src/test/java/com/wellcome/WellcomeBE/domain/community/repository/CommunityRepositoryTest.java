package com.wellcome.WellcomeBE.domain.community.repository;

import com.wellcome.WellcomeBE.QuerydslConfig;
import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.support.Support;
import com.wellcome.WellcomeBE.domain.support.repository.SupportRepository;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.global.type.Role;
import com.wellcome.WellcomeBE.global.type.SocialLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(QuerydslConfig.class)
public class CommunityRepositoryTest {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TripPlanRepository tripPlanRepository;

    @Autowired
    private SupportRepository supportRepository;

    @BeforeEach
    void setUp(){
        Member member = createTestMember("member", 12345L);
        memberRepository.save(member);

        // 후기 게시글 생성
        for(int i = 0; i < 5; i++) {
            TripPlan tripPlan = createTestTripPlan("폴더" + (i + 1), member);
            tripPlanRepository.save(tripPlan);

            Community community = createTestCommunity(member, tripPlan, "제목" + (i + 1), "내용");
            communityRepository.save(community);

            // 후기 게시글 공감 생성
            // Community1 : 5 / Community2 : 10 / Community3, Community4, Community5 : 0
            if(i < 2){
                List<Support> supports = new ArrayList<>();
                for(int j = 0; j < 5 * (i+1); j++){
                    supports.add(Support.createCommunitySupport(community, member));
                }
                supportRepository.saveAll(supports);
            }
        }
    }

    @Test
    void findByPostTypeOrderBySupportCount_후기_게시글_추천순_조회(){
        int page = 0;
        PageRequest pageRequest = PageRequest.of(page, 3);
        Page<Community> result = communityRepository.findByPostTypeOrderBySupportCount(pageRequest, Community.PostType.TRIP_PLAN);

        assertEquals(5, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertFalse(result.hasPrevious());
        assertTrue(result.hasNext());

        List<Community> content = result.getContent();
        assertEquals("제목2", content.get(0).getTitle());
        assertEquals("제목1", content.get(1).getTitle());
        assertEquals("제목5", content.get(2).getTitle());
    }

    private Member createTestMember(String nickname, Long kakaoId){
        return Member.builder()
                .nickname(nickname)
                .profileImg("profileImgUrl")
                .role(Role.USER)
                .socialType(SocialLogin.KAKAO)
                .kakaoId(kakaoId)
                .build();
    }

    private TripPlan createTestTripPlan(String folderName, Member member){
        return TripPlan.builder()
                .title(folderName)
                .startDate(LocalDate.parse("2025-01-01"))
                .endDate(LocalDate.parse("2025-01-05"))
                .member(member)
                .isActive(true)
                .status(TripPlan.Status.ACTIVE)
                .build();
    }

    private Community createTestCommunity(Member member, TripPlan tripPlan, String title, String content){
        return Community.builder()
                .member(member)
                .tripPlan(tripPlan)
                .title(title)
                .content(content)
                .postType(Community.PostType.TRIP_PLAN)
                .view(0L)
                .build();
    }

}

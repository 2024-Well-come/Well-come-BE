package com.wellcome.WellcomeBE.domain.support.repository;

import com.wellcome.WellcomeBE.NativeSqlCreatorTestConfig;
import com.wellcome.WellcomeBE.global.config.QuerydslConfig;
import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.support.Support;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.type.Area;
import com.wellcome.WellcomeBE.global.type.Category;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({QuerydslConfig.class, NativeSqlCreatorTestConfig.class})
class SupportRepositoryTest {

    @Autowired
    private SupportRepository supportRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private WellnessInfoRepository wellnessInfoRepository;

    private Member member;
    private Community community;
    private Support support;
    private WellnessInfo wellnessInfo;

    @BeforeEach
    void setUp(){
        // null 값 제한된 필드는 채워주기
      member = Member.builder().kakaoId(1L).build();
      memberRepository.save(member);

      community = Community.builder()
                .id(2L)
                .title("테스트 커뮤니티 제목")
                .content("테스트 커뮤니티 내용")
                .postType(Community.PostType.TRIP_PLAN)
                .member(member)
                .view(10L)
                .build();
      community = communityRepository.save(community);

        // given
        Point location = createPoint(127.123456, 37.123456); // 좌표 생성
        wellnessInfo = WellnessInfo.builder()
                .id(10L)
                .title("테스트 웰니스 제목")
                .area(Area.BUSAN)
                .sigungu(Sigungu.CHEORWON)
                .thema(Thema.FOOD)
                .category(Category.EVENT)
                .location(location)
                .address("테스트 주소")
                .mapX(127.123456)
                .mapY(37.123456)
                .build();

        wellnessInfo = wellnessInfoRepository.save(wellnessInfo); // WellnessInfo 저장


        support = Support.builder()
                .member(member)
                .community(community)
                .wellnessInfo(wellnessInfo)
                .supportType(Support.SupportType.COMMUNITY)
                .build();
      supportRepository.save(support);
    }

    @Test
    public void existsByCommunityAndMember(){
        // exists 없으면 true
        boolean exists = supportRepository.existsByCommunityAndMember(community,member);
        assertThat(exists).isTrue();
    }
    @Test
    public void findByMemberAndCommunityId(){
        // when
        Optional<Support> result =supportRepository.findByMemberAndCommunityId(member,community.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMember()).isEqualTo(member);
    }

    // GeometryFactory를 클래스의 필드로 선언
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    public void findByMemberAndCommunityIdAndWellnessInfoId(){

        // when
        Optional<Support> result =supportRepository.findByMemberAndCommunityIdAndWellnessInfoId(member,community.getId(),wellnessInfo.getId());
        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCommunity().getId()).isEqualTo(community.getId());
    }
    private org.locationtech.jts.geom.Point createPoint(double x, double y) {
        return geometryFactory.createPoint(new Coordinate(x, y));
    }

}
package com.wellcome.WellcomeBE.domain.like.repository;

import com.wellcome.WellcomeBE.NativeSqlCreatorTestConfig;
import com.wellcome.WellcomeBE.global.config.QuerydslConfig;
import com.wellcome.WellcomeBE.domain.like.Liked;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.type.*;
import com.wellcome.WellcomeBE.utils.TestObjectFactory;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({QuerydslConfig.class, NativeSqlCreatorTestConfig.class})
class LikedRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WellnessInfoRepository wellnessInfoRepository;

    @Autowired
    private LikedRepository likedRepository;


    @Test
    void findThemaByMemberOrderByLikedCount(){
        // given
        // - 멤버 생성
        Member member = TestObjectFactory.createTestMember("member", 12345L);
        memberRepository.save(member);

        // - 멤버의 좋아요 내역 생성
        Map<Thema, Integer> themaTypes = new LinkedHashMap<>() {{
            put(Thema.FOOD, 4);
            put(Thema.STAY, 3);
            put(Thema.NATURE, 2);
            put(Thema.BEAUTY_SPA, 1);
        }};
        List<Liked> likedList = createTestLikes(member, themaTypes);
        likedRepository.saveAll(likedList);

        // when
        List<Thema> themaList = likedRepository.findThemaByMemberOrderByLikedCount(member);

        // then
        assertEquals("푸드", themaList.get(0).getName());
        assertEquals("스테이", themaList.get(1).getName());
        assertEquals("자연", themaList.get(2).getName());
    }

    private List<Liked> createTestLikes(Member member, Map<Thema, Integer> themaTypes){
        List<Liked> likedList = new ArrayList<>();
        int titleIdx = 1;

        for(Map.Entry<Thema, Integer> entry : themaTypes.entrySet()){
            Thema thema = entry.getKey();
            int repeat = entry.getValue();

            for(int i = 0; i < repeat; i++){
                WellnessInfo wellnessInfo = createTestWellnessInfo("장소" + titleIdx++, thema);
                wellnessInfoRepository.save(wellnessInfo);
                likedList.add(Liked.create(wellnessInfo, member));
            }
        }
        return likedList;
    }

    private WellnessInfo createTestWellnessInfo(String title, Thema thema){
        return WellnessInfo.builder()
                .title(title)
                .area(Area.GANGWON)
                .sigungu(Sigungu.SOKCHO)
                .thema(thema)
                .category(Category.TOURIST_SPOT)
                .address("test-address")
                .mapX(0.0)
                .mapY(0.0)
                .location(new GeometryFactory().createPoint(new Coordinate(0.0, 0.0)))
                .build();
    }

}
package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.QuerydslConfig;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal.WellnessInfoWithLiked;
import com.wellcome.WellcomeBE.global.type.*;
import com.wellcome.WellcomeBE.utils.TestObjectFactory;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(QuerydslConfig.class)
class WellnessInfoRepositoryTest {

    @Autowired
    private WellnessInfoRepository wellnessInfoRepository;

    @Autowired
    private MemberRepository memberRepository;

    private static final Map<Thema, Integer>themaTypes= new LinkedHashMap<>() {{
        put(Thema.FOOD, 4);
        put(Thema.STAY, 3);
        put(Thema.NATURE, 2);
        put(Thema.BEAUTY_SPA, 1);
    }};

    private static final Map<Sigungu, Integer>sigunguTypes=  new LinkedHashMap<>() {{
        put(Sigungu.GANGNEUNG, 3);
        put(Sigungu.SOKCHO, 2);
        put(Sigungu.JEONGSEON, 1);
    }};

    @Test
    void findWellnessInfoByThemaAndSigunguWithWeight_웰니스_장소_목록조회(){
        // given
        int page = 0;
        PageRequest pageRequest = PageRequest.of(page, 5);

        // - 멤버 생성
        Member member = TestObjectFactory.createTestMember("member", 12345L);
        memberRepository.save(member);

        // - 여러 테마의 웰니스 장소 생성
        wellnessInfoRepository.saveAll(createTestWellnessInfosWithThemaTypesAndView(themaTypes));

        // - 테마, 시군구 설정
        List<Thema> themaList = List.of(Thema.BEAUTY_SPA, Thema.NATURE, Thema.FOOD);
        List<Sigungu> sigunguList = List.of(Sigungu.GANGNEUNG, Sigungu.SOKCHO);
        Thema thema1 = Thema.NATURE;
        Thema thema2 = Thema.FOOD;
        Thema thema3 = Thema.BEAUTY_SPA;

        // when
        Page<WellnessInfoWithLiked> result = wellnessInfoRepository.findWellnessInfoByThemaAndSigunguWithWeight(
                pageRequest, member, themaList, sigunguList, thema1, thema2, thema3
        );

        // then
        assertEquals(5, result.getContent().size());
        assertEquals(7, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(false, result.hasPrevious());
        assertEquals(true, result.hasNext());

        assertThat(result.getContent().stream()
                .map(w -> w.getWellnessInfo().getTitle())
                .collect(Collectors.toList())
        ).containsExactly("장소9", "장소10", "장소8", "장소4", "장소3");
    }

    @Test
    void findDistinctThema_요청하는_테마리스트가_null인_경우(){
        // given
        // - 여러 테마의 웰니스 장소 생성
        wellnessInfoRepository.saveAll(createTestWellnessInfosWithThemaTypes(themaTypes));

        List<Thema> themaList = new ArrayList<>();

        // when
        List<Thema> distinctThemaList = wellnessInfoRepository.findDistinctThema(themaList);

        // then
        assertEquals(
                Set.of(Thema.FOOD, Thema.STAY, Thema.NATURE, Thema.BEAUTY_SPA),
                new HashSet<>(distinctThemaList)
        );
    }

    @Test
    void findDistinctThema_요청하는_테마리스트가_null이_아닌_경우1(){
        // given
        // - 여러 테마의 웰니스 장소 생성
        wellnessInfoRepository.saveAll(createTestWellnessInfosWithThemaTypes(themaTypes));

        List<Thema> themaList = List.of(Thema.FOOD, Thema.NATURE);

        // when
        List<Thema> distinctThemaList = wellnessInfoRepository.findDistinctThema(themaList);

        // then
        assertEquals(
                Set.of(Thema.FOOD, Thema.NATURE),
                new HashSet<>(distinctThemaList)
        );
    }

    @Test
    void findDistinctThema_요청하는_테마리스트가_null이_아닌_경우2(){
        // given
        // - 여러 테마의 웰니스 장소 생성
        wellnessInfoRepository.saveAll(createTestWellnessInfosWithThemaTypes(themaTypes));

        // (Thema.HEALING_MEDITATION 테마는 테스트 데이터에 없는 테마이므로 조회되지 않는다.)
        List<Thema> themaList = List.of(Thema.FOOD, Thema.HEALING_MEDITATION);

        // when
        List<Thema> distinctThemaList = wellnessInfoRepository.findDistinctThema(themaList);

        // then
        assertEquals(
                Set.of(Thema.FOOD),
                new HashSet<>(distinctThemaList)
        );
    }

    @Test
    void findDistinctSigungu_요청하는_시군구리스트가_null인_경우(){
        // given
        // - 여러 시군구의 웰니스 장소 생성
        wellnessInfoRepository.saveAll(createTestWellnessInfosWithSigunguTypes(sigunguTypes));

        List<Sigungu> sigunguList = new ArrayList<>();

        // when
        List<Sigungu> distinctSigunguList = wellnessInfoRepository.findDistinctSigungu(sigunguList);

        // then
        assertEquals(
                Set.of(Sigungu.GANGNEUNG, Sigungu.SOKCHO, Sigungu.JEONGSEON),
                new HashSet<>(distinctSigunguList)
        );
    }

    @Test
    void findDistinctSigungu_요청하는_시군구리스트가_null이_아닌_경우1(){
        // given
        // - 여러 시군구의 웰니스 장소 생성
        wellnessInfoRepository.saveAll(createTestWellnessInfosWithSigunguTypes(sigunguTypes));

        List<Sigungu> sigunguList = List.of(Sigungu.GANGNEUNG, Sigungu.SOKCHO);

        // when
        List<Sigungu> distinctSigunguList = wellnessInfoRepository.findDistinctSigungu(sigunguList);

        // then
        assertEquals(
                Set.of(Sigungu.GANGNEUNG, Sigungu.SOKCHO),
                new HashSet<>(distinctSigunguList)
        );
    }

    @Test
    void findDistinctSigungu_요청하는_시군구리스트가_null이_아닌_경우2(){
        // given
        // - 여러 시군구의 웰니스 장소 생성
        wellnessInfoRepository.saveAll(createTestWellnessInfosWithSigunguTypes(sigunguTypes));

        // (Sigungu.PYEONGCHANG 시군구는 테스트 데이터에 없는 시군구이므로 조회되지 않는다.)
        List<Sigungu> sigunguList = List.of(Sigungu.GANGNEUNG, Sigungu.PYEONGCHANG);

        // when
        List<Sigungu> distinctSigunguList = wellnessInfoRepository.findDistinctSigungu(sigunguList);

        // then
        assertEquals(
                Set.of(Sigungu.GANGNEUNG),
                new HashSet<>(distinctSigunguList)
        );
    }

    private List<WellnessInfo> createTestWellnessInfosWithThemaTypesAndView(Map<Thema, Integer> themaTypes){
        List<WellnessInfo> wellnessInfoList = new ArrayList<>();
        int titleIdx = 1;

        for(Map.Entry<Thema, Integer> entry : themaTypes.entrySet()){
            Thema thema = entry.getKey();
            int repeat = entry.getValue();

            for(int i = 0; i < repeat; i++){
                WellnessInfo wellnessInfo = createTestWellnessInfo("장소" + titleIdx, Sigungu.SOKCHO, thema, 5 * titleIdx++);
                wellnessInfoList.add(wellnessInfo);
            }
        }
        return wellnessInfoList;
    }

    private List<WellnessInfo> createTestWellnessInfosWithThemaTypes(Map<Thema, Integer> themaTypes){
        List<WellnessInfo> wellnessInfoList = new ArrayList<>();
        int titleIdx = 1;

        for(Map.Entry<Thema, Integer> entry : themaTypes.entrySet()){
            Thema thema = entry.getKey();
            int repeat = entry.getValue();

            for(int i = 0; i < repeat; i++){
                WellnessInfo wellnessInfo = createTestWellnessInfo("장소" + titleIdx++, Sigungu.SOKCHO, thema, 0);
                wellnessInfoList.add(wellnessInfo);
            }
        }
        return wellnessInfoList;
    }

    private List<WellnessInfo> createTestWellnessInfosWithSigunguTypes(Map<Sigungu, Integer> sigunguTypes){
        List<WellnessInfo> wellnessInfoList = new ArrayList<>();
        int titleIdx = 1;

        for(Map.Entry<Sigungu, Integer> entry : sigunguTypes.entrySet()){
            Sigungu sigungu = entry.getKey();
            int repeat = entry.getValue();

            for(int i = 0; i < repeat; i++){
                WellnessInfo wellnessInfo = createTestWellnessInfo("장소" + titleIdx++, sigungu, Thema.NATURE, 0);
                wellnessInfoList.add(wellnessInfo);
            }
        }
        return wellnessInfoList;
    }

    private WellnessInfo createTestWellnessInfo(String title, Sigungu sigungu, Thema thema, long view){
        return WellnessInfo.builder()
                .title(title)
                .area(Area.GANGWON)
                .sigungu(sigungu)
                .thema(thema)
                .category(Category.TOURIST_SPOT)
                .address("temp-address")
                .mapX(0.0)
                .mapY(0.0)
                .location(new GeometryFactory().createPoint(new Coordinate(0.0, 0.0)))
                .view(view)
                .build();
    }

}
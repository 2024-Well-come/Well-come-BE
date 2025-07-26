package com.wellcome.WellcomeBE.domain.tripPlanPlace.repository;

import com.wellcome.WellcomeBE.NativeSqlCreatorTestConfig;
import com.wellcome.WellcomeBE.global.config.QuerydslConfig;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.type.*;
import com.wellcome.WellcomeBE.utils.TestObjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({QuerydslConfig.class, NativeSqlCreatorTestConfig.class})
public class TripPlanPlaceRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TripPlanRepository tripPlanRepository;

    @Autowired
    private TripPlanPlaceRepository tripPlanPlaceRepository;

    @Autowired
    private WellnessInfoRepository wellnessInfoRepository;

    private static final Map<Thema, Integer>themaTypes= new LinkedHashMap<>() {{
        put(Thema.FOOD, 4);
        put(Thema.STAY, 3);
        put(Thema.NATURE, 2);
        put(Thema.BEAUTY_SPA, 1);
    }};

    private Member member;

    @BeforeEach
    void setUp(){
        // - 멤버 생성
        member = TestObjectFactory.createTestMember("member", 12345L);
        memberRepository.save(member);
    }

    @Test
    void findByTripPlanIdAndThema_여행폴더에_저장된_모든_테마의_여행지_목록조회(){
        // given
        int page = 0;
        PageRequest pageRequest = PageRequest.of(page, 5);

        // - 여행 폴더 생성
        TripPlan tripPlan = createTestTripPlan("test-tripPlan", member);
        tripPlanRepository.save(tripPlan);

        // - 여행 폴더 내 여행지 생성
        tripPlanPlaceRepository.saveAll(createTestTripPlanPlaces(tripPlan,themaTypes));

        // when
        Page<TripPlanPlace> result = tripPlanPlaceRepository.findByTripPlanIdAndThema(pageRequest, tripPlan.getId(), null);

        // then
        assertEquals(5, result.getContent().size());
        assertEquals(10, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(false, result.hasPrevious());
        assertEquals(true, result.hasNext());
    }

    @Test
    void findByTripPlanIdAndThema_여행폴더에_저장된_특정_테마의_여행지_목록조회(){
        // given
        int page = 0;
        PageRequest pageRequest = PageRequest.of(page, 5);

        // - 여행 폴더 생성
        TripPlan tripPlan = createTestTripPlan("test-tripPlan", member);
        tripPlanRepository.save(tripPlan);

        // - 여행 폴더 내 여행지 생성
        tripPlanPlaceRepository.saveAll(createTestTripPlanPlaces(tripPlan,themaTypes));

        // when
        Page<TripPlanPlace> result = tripPlanPlaceRepository.findByTripPlanIdAndThema(pageRequest, tripPlan.getId(), Thema.FOOD);

        // then
        assertEquals(4, result.getContent().size());
        assertTrue(result.getContent().stream()
                .allMatch(tpp -> Thema.FOOD.equals(tpp.getWellnessInfo().getThema())));

        assertEquals(4, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(false, result.hasPrevious());
        assertEquals(false, result.hasNext());
    }

    @Test
    void findByWellnessInfoAndReviewConditionsExist_웰니스장소에_대해_개별후기가_작성된_여행폴더_조회(){
        // given
        // - 웰니스 장소 생성
        WellnessInfo wellnessInfo = createTestWellnessInfo("장소", Thema.FOOD);
        wellnessInfoRepository.save(wellnessInfo);

        // - 여행 폴더 생성, 여행지 생성
        for(int i = 0; i < 10; i++){
            Integer rating = null;
            String review = null;

            TripPlan tripPlan = createTestTripPlan("여행 폴더" + (i+1), member);
            tripPlanRepository.save(tripPlan);

            if(i % 2 == 0) rating = 5;
            if(i % 3 == 0) review = "리뷰";
            TripPlanPlace tripPlanPlace = createTestTripPlanPlace(tripPlan, wellnessInfo, rating, review);
            tripPlanPlaceRepository.save(tripPlanPlace);
        }

        // when
        List<TripPlan> tripPlanList = tripPlanPlaceRepository.findByWellnessInfoAndReviewConditionsExist(wellnessInfo);

        // then
        assertEquals(7, tripPlanList.size()); // 0, 2, 3, 4, 6, 8, 9
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

    private TripPlanPlace createTestTripPlanPlace(TripPlan tripPlan, WellnessInfo wellnessInfo, Integer rating, String review){
        return TripPlanPlace.builder()
                .tripPlan(tripPlan)
                .wellnessInfo(wellnessInfo)
                .rating(rating)
                .review(review)
                .build();
    }

    private List<TripPlanPlace> createTestTripPlanPlaces(TripPlan tripPlan, Map<Thema, Integer> themaTypes){
        List<TripPlanPlace> tripPlanPlaceList = new ArrayList<>();
        int titleIdx = 1;

        for(Map.Entry<Thema, Integer> entry : themaTypes.entrySet()){
            Thema thema = entry.getKey();
            int repeat = entry.getValue();

            for(int i = 0; i < repeat; i++){
                WellnessInfo wellnessInfo = createTestWellnessInfo("장소" + titleIdx++, thema);
                wellnessInfoRepository.save(wellnessInfo);

                tripPlanPlaceList.add(createTestTripPlanPlace(tripPlan, wellnessInfo, null, null));
            }
        }
        return tripPlanPlaceList;
    }

    private WellnessInfo createTestWellnessInfo(String title, Thema thema){
        return WellnessInfo.builder()
                .title(title)
                .area(Area.GANGWON)
                .sigungu(Sigungu.SOKCHO)
                .thema(thema)
                .category(Category.TOURIST_SPOT)
                .address("temp-address")
                .mapX(0.0)
                .mapY(0.0)
                .location(new GeometryFactory().createPoint(new Coordinate(0.0, 0.0)))
                .build();
    }

}

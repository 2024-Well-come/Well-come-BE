package com.wellcome.WellcomeBE.domain.wellnessInfo.home;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final WellnessInfoRepository wellnessInfoRepository;

    private static final List<Long> RECOMMENDED_PLACE_LIST = List.of(7L, 333L, 864L, 921L, 1088L, 1105L, 1115L);

    public HomeResponse getRandomWellnessInfoList() {

        // WellnessInfo 식별자 랜덤 선택
        List<Long> wellnessInfoIdList = getRandomPlaceIdList(RECOMMENDED_PLACE_LIST, 2); // 랜덤으로 보여줄 장소 수: 2개 설정

        // WellnessInfo 조회
        List<HomeResponse.RandomWellnessInfo> wellnessInfoList = wellnessInfoRepository.findByIdIn(wellnessInfoIdList).stream()
                .map(HomeResponse.RandomWellnessInfo::from)
                .collect(Collectors.toList());

        return HomeResponse.from(wellnessInfoList);
    }

    private List<Long> getRandomPlaceIdList(List<Long> placeList, int count) {
        if (placeList.size() < count) {
            throw new IllegalArgumentException("RECOMMENDED_PLACE_LIST의 size가 요청된 count 보다 작습니다.");
        }

        List<Long> shuffledList = new ArrayList<>(placeList);
        Collections.shuffle(shuffledList);
        return shuffledList.subList(0, count); // 섞인 리스트에서 처음 count 개수만큼 선택
    }

}

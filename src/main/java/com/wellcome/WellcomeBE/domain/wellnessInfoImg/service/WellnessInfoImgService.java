package com.wellcome.WellcomeBE.domain.wellnessInfoImg.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImg;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.response.TourImageApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WellnessInfoImgService {

    private final WebClient tourImageApiWebClient;

    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;

    public void fetchAndSaveTourImg() {
        // DB WellnessInfo에 해당하는 Img 저장
        List<WellnessInfo> wellnessInfoList = wellnessInfoRepository.findAll(); // DB에서 모든 데이터를 조회

        Flux.fromIterable(wellnessInfoList) // DB 조회 결과에 대해 Iterable 사용
                .flatMap(this::fetchAndSaveImage) // 이미지 API 호출 및 저장
                .collectList()
                .block();
    }

    // 이미지 API 호출 및 DB에 저장하는 메서드
    private Mono<Void> fetchAndSaveImage(WellnessInfo wellnessInfo) {
        return fetchImage(wellnessInfo.getContent()) // 이미지 API 호출
                .flatMapMany(response -> Flux.fromIterable(response.getBody().getItems().getItem())) // API 응답에서 이미지 리스트 추출
                .map(imgItem -> WellnessInfoImg.builder() // WellnessInfoImg 엔티티 생성
                        .wellnessInfo(wellnessInfo)
                        .imgUrl(imgItem.getOriginimgurl()) // API에서 반환된 이미지 URL 사용
                        .build())
                .collectList()
                .doOnNext(wellnessInfoImgRepository::saveAll) // 생성된 엔티티 리스트 저장
                .then(); // Mono<Void> 반환
    }

    // tour4.0 Image API 요청
    private Mono<TourImageApiResponse> fetchImage(String wellnessInfoId) {
        return tourImageApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("contentId", wellnessInfoId) // contentId를 쿼리 파라미터로 추가
                        .build())
                .retrieve()
                .bodyToMono(TourImageApiResponse.class);
    }


}

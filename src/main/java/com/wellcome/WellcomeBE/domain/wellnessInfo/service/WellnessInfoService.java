package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoService {

    private final WebClient tourBasicApiWebClient;
    private final WellnessInfoRepository wellnessInfoRepository;

    private static final String GANGWONDO_AREACODE = "32";
    private static final int NUM_OF_ROWS = 100;

    public void fetchAndSaveTourInfo() {
        Flux.fromIterable(CategoryDetail.CATEGORY_PARAM_LIST)
                .flatMap(categoryDetail -> fetchData(categoryDetail, 1))
                .buffer(50)
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .collectList()
                .block();
    }

    private Flux<WellnessInfo> fetchData(CategoryDetail categoryDetail, int pageNo) {
        return fetchFromTourBasicApi(categoryDetail, 1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS < totalCount) {
                        return fetchFromTourBasicApi(categoryDetail, currentPage + 1);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(response -> {
                    TourBasicApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    if (items == null || items.getItem() == null || items.getItem().isEmpty()) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(items.getItem());
                })
                .map(this::convertToEntity);
    }

    private Mono<TourBasicApiResponse> fetchFromTourBasicApi(CategoryDetail categoryDetail, int pageNo){
        return tourBasicApiWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam("areaCode", GANGWONDO_AREACODE)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", NUM_OF_ROWS)
                            .queryParam("cat1", categoryDetail.getCat1());

                    if (categoryDetail.getCat2() != null) {
                        uriBuilder.queryParam("cat2", categoryDetail.getCat2());
                    }
                    if (categoryDetail.getCat3() != null) {
                        uriBuilder.queryParam("cat3", categoryDetail.getCat3());
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(TourBasicApiResponse.class);
    }

    private WellnessInfo convertToEntity(TourBasicApiResponse.Response.Body.Items.Item item){
        try {
            // TODO 테마 분류 필요 (NATURE로 임시 지정)
            return item.toEntity(Thema.NATURE);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: ", e);
        }
    }

}

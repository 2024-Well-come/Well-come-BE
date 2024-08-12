package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS;

@Service
@Slf4j
public class WellnessInfoService {

    private final WebClient tourBasicApiWebClient;
    private final WebClient tourSearchApiWebClient;
    private final WellnessInfoRepository wellnessInfoRepository;

    private static final String GANGWONDO_AREACODE = "32";
    private static final int NUM_OF_ROWS_BASIC = 100;
    private static final int NUM_OF_ROWS_SEARCH = 5;

    public WellnessInfoService(
            @Qualifier("tourBasicApiWebClient") WebClient tourBasicApiWebClient,
            @Qualifier("tourSearchApiWebClient") WebClient tourSearchApiWebClient,
            WellnessInfoRepository wellnessInfoRepository) {
        this.tourBasicApiWebClient = tourBasicApiWebClient;
        this.tourSearchApiWebClient = tourSearchApiWebClient;
        this.wellnessInfoRepository = wellnessInfoRepository;
    }

    public void fetchAndSaveTourInfo() {

        // Basic API 호출 및 저장
        Flux.fromIterable(CategoryDetail.CATEGORY_PARAM_LIST)
                .flatMap(this::fetchData)
                .buffer(50)
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .collectList()
                .block();

        // Search API 호출 및 저장
        Flux.fromIterable(KEYWORDS)
                .flatMap(this::fetchDataByKeyword)
                .collectList()
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .block();
    }

    private Flux<WellnessInfo> fetchData(CategoryDetail categoryDetail) {
        return fetchFromTourBasicApi(categoryDetail, 1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS_BASIC < totalCount) {
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
                            .queryParam("numOfRows", NUM_OF_ROWS_BASIC)
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

    private Flux<WellnessInfo> fetchDataByKeyword(String keyword) {
        return fetchDataFromTourSearchApi(keyword, 1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS_SEARCH < totalCount) {
                        return fetchDataFromTourSearchApi(keyword, currentPage + 1);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(response -> {
                    TourBasicApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    if (items == null || items.getItem() == null || items.getItem().isEmpty()) {
                        log.info("keyword '{}'에 대한 검색 결과 없음", keyword);
                        return Flux.empty();
                    }
                    return Flux.fromIterable(response.getResponse().getBody().getItems().getItem());
                })
                .map(this::convertToEntity);
    }

    private Mono<TourBasicApiResponse> fetchDataFromTourSearchApi(String keyword, int pageNo) {
        return tourSearchApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("areaCode", GANGWONDO_AREACODE)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", NUM_OF_ROWS_SEARCH)
                        .queryParam("keyword", keyword)
                        .build())
                .retrieve()
                .bodyToMono(TourBasicApiResponse.class);
    }

    private WellnessInfo convertToEntity(TourBasicApiResponse.Response.Body.Items.Item item){
        try {
            return item.toEntity();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: ", e);
        }
    }

}

package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoService {

    private final WebClient tourBasicApiWebClient;
    private final WellnessInfoRepository wellnessInfoRepository;

    private static final String GANGWONDO_AREACODE = "32";
    private static final int NUM_OF_ROWS = 100;

    public void fetchAndSaveTourInfo() {
        fetchFromTourBasicApi(1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS < totalCount) {
                        return fetchFromTourBasicApi(currentPage + 1);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(response -> Flux.fromIterable(response.getResponse().getBody().getItems().getItem()))
                .map(this::convertToEntity)
                .buffer(50)
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .collectList()
                .block();
    }

    private Mono<TourBasicApiResponse> fetchFromTourBasicApi(int pageNo){
        return tourBasicApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("areaCode", GANGWONDO_AREACODE)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", NUM_OF_ROWS)
                        .build())
                .retrieve()
                .bodyToMono(TourBasicApiResponse.class);
    }

    private WellnessInfo convertToEntity(TourBasicApiResponse.Response.Body.Items.Item item){
        try {
            return item.toEntity(Thema.NATURE);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: ", e);
        }
    }

}

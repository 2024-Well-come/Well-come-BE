package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class CategoryApiFetchService extends WellnessInfoFetchTemplate<CategoryDetail> {
    private final TourInfoApiWebClientConfig webClientConfig;
    private static final int NUM_OF_ROWS_BASIC = 100;

    public CategoryApiFetchService(
            WebClient webClient, WellnessInfoRepository wellnessInfoRepository,
            TourInfoApiWebClientConfig webClientConfig
    ){
        super(webClient, wellnessInfoRepository);
        this.webClientConfig = webClientConfig;
    }

    @Override
    protected String getLabel(CategoryDetail category) {
        return category.getCat1() + " " + category.getCat2() + " " + category.getCat3();
    }

    @Override
    protected int getNumOfRows(){
        return NUM_OF_ROWS_BASIC;
    }

    @Override
    protected String constructUri(Map<String, String> params, CategoryDetail categoryDetail, int pageNo) {

        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(NUM_OF_ROWS_BASIC));
        params.put("cat1", categoryDetail.getCat1());
        if (categoryDetail.getCat2() != null) {
            params.put("cat2", categoryDetail.getCat2());
        }
        if (categoryDetail.getCat3() != null) {
            params.put("cat3", categoryDetail.getCat3());
        }

        return webClientConfig.getTourBasicApiUrl(params);
    }
}

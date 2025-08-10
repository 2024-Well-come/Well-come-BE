package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_RESPONSE_ERROR;

@Service
public class KeywordApiFetchService extends WellnessInfoFetchTemplate<String> {
    private final TourInfoApiWebClientConfig webClientConfig;
    private static final int NUM_OF_ROWS_SEARCH = 10;

    public KeywordApiFetchService(
            WebClient webClient, WellnessInfoRepository wellnessInfoRepository,
            TourInfoApiWebClientConfig webClientConfig
    ) {
        super(webClient, wellnessInfoRepository);
        this.webClientConfig = webClientConfig;
    }

    @Override
    protected String getLabel(String keyword) {
        return keyword;
    }

    @Override
    protected int getNumOfRows() {
        return NUM_OF_ROWS_SEARCH;
    }

    @Override
    protected String constructUri(Map<String, String> params, String keyword, int pageNo) {

        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(NUM_OF_ROWS_SEARCH));

        // 파라미터 값 UTF-8로 인코딩
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
            params.put("keyword", encodedKeyword);
        } catch (UnsupportedEncodingException e) {
            throw new CustomException(TOUR_API_RESPONSE_ERROR, "Encoding failed for keyword");
        }

        return webClientConfig.getTourSearchApiUrl(params);
    }
}

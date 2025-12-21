package com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.keyword;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfoTemp;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.template.WellnessInfoSaver;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service("keywordApiTempService")
public class KeywordApiTempService extends KeywordApiFetchService {
    public KeywordApiTempService(
            WebClient webClient, TourInfoApiWebClientConfig webClientConfig,
            @Qualifier("tempSaver") WellnessInfoSaver<WellnessInfoTemp> saver
    ){
        super(webClient, saver, webClientConfig);
    }
}

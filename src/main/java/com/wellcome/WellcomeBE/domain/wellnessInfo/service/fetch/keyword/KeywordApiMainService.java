package com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.keyword;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.template.WellnessInfoSaver;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service("keywordApiMainService")
public class KeywordApiMainService extends KeywordApiFetchService {
    public KeywordApiMainService(
            WebClient webClient, TourInfoApiWebClientConfig webClientConfig,
            @Qualifier("mainSaver") WellnessInfoSaver<WellnessInfo> saver
    ){
        super(webClient, saver, webClientConfig);
    }
}

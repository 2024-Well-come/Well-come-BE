package com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.template;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.io.ParseException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("mainSaver")
@RequiredArgsConstructor
public class WellnessInfoMainSaver implements WellnessInfoSaver<WellnessInfo> {

    private final WellnessInfoRepository wellnessInfoRepository;

    @Override
    public WellnessInfo convertEntity(TourBasicApiResponse.Response.Body.Items.Item item) throws ParseException {
        return item.toEntity();
    }

    @Override
    public void saveAll(List<WellnessInfo> entities) {
        wellnessInfoRepository.saveAll(entities);
    }
}

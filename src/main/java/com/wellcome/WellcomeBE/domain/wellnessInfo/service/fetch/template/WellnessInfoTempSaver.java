package com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.template;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfoTemp;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoTempRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.io.ParseException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("tempSaver")
@RequiredArgsConstructor
public class WellnessInfoTempSaver implements WellnessInfoSaver<WellnessInfoTemp> {

    private final WellnessInfoTempRepository wellnessInfoTempRepository;

    @Override
    public WellnessInfoTemp convertEntity(TourBasicApiResponse.Response.Body.Items.Item item) throws ParseException {
        return item.toTempEntity();
    }

    @Override
    public void saveAll(List<WellnessInfoTemp> entities) {
        wellnessInfoTempRepository.saveAll(entities);
    }
}

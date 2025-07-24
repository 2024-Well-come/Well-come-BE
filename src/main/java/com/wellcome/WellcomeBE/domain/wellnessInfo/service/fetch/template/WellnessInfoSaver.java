package com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.template;

import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import org.locationtech.jts.io.ParseException;

import java.util.List;

public interface WellnessInfoSaver<T> {
    T convertEntity(TourBasicApiResponse.Response.Body.Items.Item item) throws ParseException;

    void saveAll(List<T> entities);
}

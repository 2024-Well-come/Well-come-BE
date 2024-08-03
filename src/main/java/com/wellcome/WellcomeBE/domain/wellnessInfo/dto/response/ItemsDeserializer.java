package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class ItemsDeserializer extends JsonDeserializer<TourBasicApiResponse.Response.Body.Items> {

    @Override
    public TourBasicApiResponse.Response.Body.Items deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isNull() || (node.isTextual() && node.asText().isEmpty())) {
            return null; // 빈 문자열이나 null인 경우 null 반환
        }
        return p.getCodec().treeToValue(node, TourBasicApiResponse.Response.Body.Items.class);
    }

}

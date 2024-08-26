package com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ItemsDeserializer extends JsonDeserializer<TourImageApiResponse.Response.Body.Items> {

    @Override
    public TourImageApiResponse.Response.Body.Items deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = mapper.readTree(jp);

        // 노드가 객체인지 문자열인지 확인
        if (node.isObject()) {
            return mapper.treeToValue(node, TourImageApiResponse.Response.Body.Items.class);
        } else if (node.isTextual() && node.asText().isEmpty()) {
            // 노드가 빈 문자열이면 빈 Items 객체 반환
            return new TourImageApiResponse.Response.Body.Items();
        } else {
            throw new IOException("Unexpected value for items: " + node.toString());
        }
    }
}
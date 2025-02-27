package com.wellcome.WellcomeBE.domain.support.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.domain.support.dto.SupportRequest;
import com.wellcome.WellcomeBE.domain.support.service.SupportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = SupportController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class) // JPA 관련 Bean을 Mock 처리
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SupportService supportService;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void createCommunitySupport() throws Exception {
        // then
        mockMvc.perform(post("/api/support/community/{communityId}", 1L))
                .andExpect(status().isOk());
        verify(supportService, Mockito.times(1)).createCommunitySupport(1L);
    }

    @Test
    void deleteCommunitySupport() throws Exception {
        //then
        mockMvc.perform(delete("/api/support/community/{communityId}",1L))
                .andExpect(status().isOk());

        verify(supportService, Mockito.times(1)).deleteCommunitySupport(1L);
    }

    @Test
    void createCommunityInWellnessSupport() throws Exception {
        // given
        SupportRequest.CreateCommunityInWellnessSupportRequestDto request
                =SupportRequest.CreateCommunityInWellnessSupportRequestDto.builder()
                .communityId(1L)
                .wellnessInfoId(10L)
                .build();
        String content = objectMapper.writeValueAsString(request);

        // when - then
        mockMvc.perform(post("/api/support/community/wellness")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk());
        Mockito.verify(supportService, Mockito.times(1)).createTripPlanPlaceSupport(any(SupportRequest.CreateCommunityInWellnessSupportRequestDto.class)); //any또는 any(클래스)
    }

    @Test
    void deleteCommunityInWellnessSupport() throws Exception{
        //given
        SupportRequest.DeleteCommunityInWellnessSupportRequestDto request =
                SupportRequest.DeleteCommunityInWellnessSupportRequestDto.builder()
                .communityId(1L)
                .wellnessInfoId(10L)
                .build();
        String content = objectMapper.writeValueAsString(request);

        // when-then
        mockMvc.perform(delete("/api/support/community/wellness")
                        .contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk());
        verify(supportService,Mockito.times(1)).deleteCommunityInWellnessSupport(any());

    }
}
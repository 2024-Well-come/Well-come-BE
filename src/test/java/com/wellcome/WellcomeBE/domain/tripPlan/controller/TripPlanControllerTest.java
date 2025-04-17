package com.wellcome.WellcomeBE.domain.tripPlan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanCreateRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlan.service.TripPlanService;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.service.TripPlanPlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TripPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class)
public class TripPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripPlanService tripPlanService;
    @MockBean
    private TripPlanPlaceService tripPlanPlaceService;

    @MockBean
    private TripPlanRepository tripPlanRepository;

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy.MM.dd"));
    }

    @Test
    void testModifyTripPlan_InvalidRequest() throws Exception {
        // name 필드가 비어있어 @NotBlank가 작동해야 하는 경우
        TripPlanCreateRequest invalidRequest = TripPlanCreateRequest.builder()
                .name("")  // 비어있는 이름
                .tripStartDate(null)
                .tripEndDate(null)
                .build();

        mockMvc.perform(patch("/api/my-trips/plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())  // 400 CustomError 설정값
                .andExpect(jsonPath("$.errorCode").value("1002")) // 오류 코드 검증
                .andExpect(jsonPath("$.message").value("name : 여행 계획의 이름은 필수입니다.")); // 오류 메시지 검증
    }

    @Test
    void testModifyTripPlan_ValidRequest() throws Exception {
        // 날짜 포맷 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        TripPlanCreateRequest validRequest = TripPlanCreateRequest.builder()
                .name("Test Plan")
                .tripStartDate(null)
                .tripEndDate(null)
//                .tripStartDate(LocalDate.parse("2025.04.01", formatter))
//                .tripEndDate(LocalDate.parse("2025.04.10", formatter))
                .build();

        // TripPlanService의 메소드 호출에 대한 mock 설정
        doNothing().when(tripPlanService).updateTripPlan(anyLong(), any(TripPlanCreateRequest.class));

        // 테스트 수행
        mockMvc.perform(patch("/api/my-trips/plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());  // 200 OK

        // updateTripPlan 메소드가 호출되었는지 확인
        verify(tripPlanService).updateTripPlan(anyLong(), any(TripPlanCreateRequest.class));
    }
}

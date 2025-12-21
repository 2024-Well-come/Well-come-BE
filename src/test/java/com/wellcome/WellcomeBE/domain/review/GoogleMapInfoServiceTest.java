package com.wellcome.WellcomeBE.domain.review;

import com.wellcome.WellcomeBE.domain.review.dto.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.review.service.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class) // Mockito 확장 기능 활성화
class GoogleMapInfoServiceTest {

    @Mock
    private RedisTemplate<String,Object> jsonRedisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private WebClient webClient;
    @Mock //URI 설정
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock //요청 헤더를 설정하고 최종적으로 요청을 보내는 역할
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @InjectMocks
    private GoogleMapInfoService googleMapInfoService;
    @Mock
    private WellnessInfoRepository wellnessInfoRepository;

    private MockWebServer mockWebServer;

    private static final String PLACE_ID = "test_place_id";
    private PlaceReviewResponse mockReviewResponse;


    @BeforeEach
    void setUp() {
        PlaceReviewResponse.PlaceResult placeResult = new PlaceReviewResponse.PlaceResult();
        placeResult.setName("Test Place");
        placeResult.setFormatted_phone_number("123-4567");
        placeResult.setRating(4.5);
        placeResult.setWebsite("www.test.com");
        placeResult.setUser_ratings_total(100);

        mockReviewResponse = PlaceReviewResponse.builder()
                .result(placeResult)
                .status("OK")
                .error_message(null)
                .html_attributions(Collections.emptyList())
                .build();

        when(jsonRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 구글리뷰_캐시에_데이터가_있을_때_CacheHit(){
        //given
        when(valueOperations.get(PLACE_ID)).thenReturn(mockReviewResponse);

        // when
        Mono<PlaceReviewResponse> resultMono = googleMapInfoService.getPlaceDetails(PLACE_ID);
        PlaceReviewResponse result = resultMono.block();

        //then 캐싱 데이터가 있으면 호출이 업어야 함
        assertNotNull(result);
        assertEquals(mockReviewResponse, result);
        verify(webClient,never()).get();
    }

    @Test
    void 구글리뷰_캐시에_데이터가_없을_때_CacheMiss() {
        // given 캐시에서 데이터가 없음
        when(valueOperations.get(PLACE_ID)).thenReturn(null);

        // WebClient mock 설정
        when(webClient.get()).thenReturn(requestHeadersUriSpec); // webClient.get()이 requestHeadersUriSpec을 반환하도록 설정
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec); // uri()에 Function<UriBuilder, URI> 타입을 명확히 설정
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec); // retrieve() 호출 시 responseSpec 반환
        when(responseSpec.bodyToMono(PlaceReviewResponse.class)).thenReturn(Mono.just(mockReviewResponse)); // bodyToMono() 호출 시 mockReviewResponse 반환

        // when
        Mono<PlaceReviewResponse> resultMono = googleMapInfoService.getPlaceDetails(PLACE_ID);
        PlaceReviewResponse result = resultMono.block();
        // then
        assertNotNull(result);
        assertEquals(mockReviewResponse, result);
        verify(valueOperations).set(eq(PLACE_ID), eq(mockReviewResponse), eq(Duration.ofDays(7)));  // 캐시에 저장된 내용 확인
    }


    @BeforeEach
    void setUp2() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

    }

    @Test
    void GoogleMaps_외부_API_호출_검증_200() {
        // MockWebServer 200 응답
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"status\": \"OK\", \"result\": {\"name\": \"Test Place\"}}")
                .addHeader("Content-Type", "application/json"));

        //given WebClient의 URL을 mockWebServer URL로 설정
        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.create(baseUrl);

        GoogleMapInfoService googleMapInfoService = new GoogleMapInfoService(webClient, jsonRedisTemplate, wellnessInfoRepository);

        //when API 호출
        Mono<PlaceReviewResponse> resultMono = googleMapInfoService.getPlaceDetails(PLACE_ID);
        PlaceReviewResponse result = resultMono.block();

        //then 결과 검증
        assertNotNull(result);
        assertEquals("Test Place", result.getResult().getName());

    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown(); // 서버 종료
    }


}
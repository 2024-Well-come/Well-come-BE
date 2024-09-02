package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoBasicResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImg;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS;

@Service
@Slf4j
public class WellnessInfoService {

    private final WebClient tourBasicApiWebClient;
    private final WebClient tourSearchApiWebClient;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;
    private final GoogleMapInfoService googleMapInfoService;
    private final LikedRepository likedRepository;

    private static final String GANGWONDO_AREACODE = "32";
    private static final int NUM_OF_ROWS_BASIC = 100;
    private static final int NUM_OF_ROWS_SEARCH = 5;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    public WellnessInfoService(
            @Qualifier("tourBasicApiWebClient") WebClient tourBasicApiWebClient,
            @Qualifier("tourSearchApiWebClient") WebClient tourSearchApiWebClient,
            WellnessInfoRepository wellnessInfoRepository,
            WellnessInfoImgRepository wellnessInfoImgRepository,
            GoogleMapInfoService googleMapInfoService,
            LikedRepository likedRepository) {
        this.tourBasicApiWebClient = tourBasicApiWebClient;
        this.tourSearchApiWebClient = tourSearchApiWebClient;
        this.wellnessInfoRepository = wellnessInfoRepository;
        this.wellnessInfoImgRepository = wellnessInfoImgRepository;
        this.googleMapInfoService = googleMapInfoService;
        this.likedRepository = likedRepository;
    }

    public void fetchAndSaveTourInfo() {

        // Basic API 호출 및 저장
        Flux.fromIterable(CategoryDetail.CATEGORY_PARAM_LIST)
                .flatMap(this::fetchData)
                .buffer(50)
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .collectList()
                .block();

        // Search API 호출 및 저장
        Flux.fromIterable(KEYWORDS)
                .flatMap(this::fetchDataByKeyword)
                .collectList()
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .block();
    }

    private Flux<WellnessInfo> fetchData(CategoryDetail categoryDetail) {
        return fetchFromTourBasicApi(categoryDetail, 1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS_BASIC < totalCount) {
                        return fetchFromTourBasicApi(categoryDetail, currentPage + 1);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(response -> {
                    TourBasicApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    if (items == null || items.getItem() == null || items.getItem().isEmpty()) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(items.getItem());
                })
                .map(this::convertToEntity);
    }

    private Mono<TourBasicApiResponse> fetchFromTourBasicApi(CategoryDetail categoryDetail, int pageNo){
        return tourBasicApiWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam("areaCode", GANGWONDO_AREACODE)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", NUM_OF_ROWS_BASIC)
                            .queryParam("cat1", categoryDetail.getCat1());

                    if (categoryDetail.getCat2() != null) {
                        uriBuilder.queryParam("cat2", categoryDetail.getCat2());
                    }
                    if (categoryDetail.getCat3() != null) {
                        uriBuilder.queryParam("cat3", categoryDetail.getCat3());
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(TourBasicApiResponse.class);
    }

    private Flux<WellnessInfo> fetchDataByKeyword(String keyword) {
        return fetchDataFromTourSearchApi(keyword, 1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS_SEARCH < totalCount) {
                        return fetchDataFromTourSearchApi(keyword, currentPage + 1);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(response -> {
                    TourBasicApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    if (items == null || items.getItem() == null || items.getItem().isEmpty()) {
                        log.info("keyword '{}'에 대한 검색 결과 없음", keyword);
                        return Flux.empty();
                    }
                    return Flux.fromIterable(response.getResponse().getBody().getItems().getItem());
                })
                .map(this::convertToEntity);
    }

    private Mono<TourBasicApiResponse> fetchDataFromTourSearchApi(String keyword, int pageNo) {
        return tourSearchApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("areaCode", GANGWONDO_AREACODE)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", NUM_OF_ROWS_SEARCH)
                        .queryParam("keyword", keyword)
                        .build())
                .retrieve()
                .bodyToMono(TourBasicApiResponse.class);
    }

    private WellnessInfo convertToEntity(TourBasicApiResponse.Response.Body.Items.Item item){
        try {
            return item.toEntity();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: ", e);
        }
    }


    /**
     *  [FEAT] 웰니스 장소 상세 조회(1) - 기본 정보 조회
     */
    public WellnessInfoBasicResponse getWellnessInfoBasic(Long wellnessInfoId){

        // 1. 웰니스 정보 가져오기
        WellnessInfo wellness = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new RuntimeException("[임시] 해당하는 웰니스가 존재하지 않습니다."));

        // 2. Google Place API를 통해 장소 세부 정보 가져오기
        PlaceReviewResponse.PlaceResult placeResult = googleMapInfoService.getPlaceDetails(wellness.getParentId()).block().getResult();

        // 3. 웰니스 이미지 목록 가져오기
        List<String> wellnessInfoImg = wellnessInfoImgRepository.findByWellnessInfo(wellness);

        // 4. 현재 요일 구하기
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String todayString = today.name(); // 예: "MONDAY"
        LocalTime now = LocalTime.now();

        // 5. JSON 데이터에서 운영 시간 찾기
        String openDetail = "정보 없음";
        Boolean isOpen = false;

        // Null 체크 추가
        if (placeResult.getOpening_hours() != null) {
            openDetail = getOpenDetail(placeResult.getOpening_hours().getWeekday_text(), todayString);
            isOpen = isCurrentlyOpen(placeResult.getOpening_hours().getPeriods(), today, now);
        }


        // 6. WellnessInfoBasicResponse 객체 생성
        return WellnessInfoBasicResponse.builder()
                .wellnessInfoId(wellness.getId())
                .thumbnailUrl(wellness.getThumbnailUrl())
                .imgList(wellnessInfoImg)
                .title(wellness.getTitle())
                .category(wellness.getCategory().getName())
                .address(wellness.getAddress())
                //.isLiked(likedRepository.findLikedByWellnessInfoAndMember())
                .isOpen(isOpen)
                .openDetail(openDetail)
                .tel(wellness.getTel())
                .website(placeResult.getWebsite())
                .build();
    }

    private String getOpenDetail(List<String> weekdayText, String todayString) {
        Map<String, String> WEEKDAY_MAP = new HashMap<>() {{
            put("MONDAY", "월요일");
            put("TUESDAY", "화요일");
            put("WEDNESDAY", "수요일");
            put("THURSDAY", "목요일");
            put("FRIDAY", "금요일");
            put("SATURDAY", "토요일");
            put("SUNDAY", "일요일");
        }};

        for (String text : weekdayText) {
            if (text.startsWith(WEEKDAY_MAP.get(todayString))) {
                return text;
            }
        }
        return "정보 없음";
    }

    private Boolean isCurrentlyOpen(List<PlaceReviewResponse.PlaceResult.OpeningHours.Period> periods, DayOfWeek today, LocalTime now) {
        int todayIndex = today.getValue(); // 월요일이 1, 일요일이 7

        for (PlaceReviewResponse.PlaceResult.OpeningHours.Period period : periods) {
            // period.getOpen().getDay()와 todayIndex가 같아야 함
            if (period.getOpen().getDay() == todayIndex - 1) { // JSON의 day는 0부터 시작하므로 1을 빼야 함
                LocalTime openTime = LocalTime.parse(period.getOpen().getTime(), TIME_FORMATTER);
                LocalTime closeTime = LocalTime.parse(period.getClose().getTime(), TIME_FORMATTER);

                if (now.isAfter(openTime) && now.isBefore(closeTime)) {
                    return true;
                }
            }
        }
        return false;
    }

}

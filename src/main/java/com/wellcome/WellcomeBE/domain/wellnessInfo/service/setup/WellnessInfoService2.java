package com.wellcome.WellcomeBE.domain.wellnessInfo.service.setup;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.category.CategoryApiMainService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.keyword.KeywordApiMainService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.utils.CompletableFutureUtils;
import com.wellcome.WellcomeBE.global.image.S3Service;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS;

@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoService2 {

    @Qualifier("categoryApiMainService")
    private final CategoryApiMainService categoryApiMainService;
    @Qualifier("keywordApiMainService")
    private final KeywordApiMainService keywordApiMainService;

    private final WellnessInfoRepository wellnessInfoRepository;
    private final S3Service s3Service;
    private final Executor asyncExecutor;

    public CompletableFuture<Void> setUpWellnessInfo(){
//        log.info("🆕 WellnessInfo 데이터 초기화 시작");

        return fetchAllData()
                .thenRun(this::uploadThumbnailImgToS3)
                .whenComplete((v, ex) -> {
                    if (ex == null) {
//                        log.info("🆕 WellnessInfo 데이터 초기화 완료");
                    } else {
                        log.error("❌ WellnessInfo 데이터 초기화 실패: error={}", ex.getMessage());
                    }
                });
    }

    // 한국관광공사 API 호출 및 데이터 저장 로직 실행
    private CompletableFuture<Void> fetchAllData() {
        long start = System.currentTimeMillis();

        // 카테고리별 API 호출
//        log.info("<< 카테고리별 API 호출 시작 >>");
        List<CompletableFuture<Integer>> categoryFutures = new ArrayList<>();
        for (CategoryDetail category : CategoryDetail.CATEGORY_PARAM_LIST) {
            categoryFutures.add(categoryApiMainService.fetchAndSave(category));
        }

        CompletableFuture<Integer> categoryFailFast = CompletableFutureUtils.sumOrFirstFail(categoryFutures)
                .whenComplete((cnt, ex) -> {
                    if (ex == null) {
//                        log.info("<< 카테고리별 API 호출 완료 >>");
                    } else {
                        log.error("<< 카테고리별 API 호출 중단: 실패 감지 >>");
                    }
                });

        // 카테고리 API 호출 성공 시, 키워드 API 호출
        return categoryFailFast.thenCompose(categorySavedCount -> {
            List<CompletableFuture<Integer>> keywordFutures = new ArrayList<>();
            for(String keyword : KEYWORDS){
                keywordFutures.add(keywordApiMainService.fetchAndSave(keyword));
            }

            CompletableFuture<Integer> keywordFailFast = CompletableFutureUtils.sumOrFirstFail(keywordFutures)
                    .whenComplete((cnt, ex) -> {
                        if (ex == null) {
//                            log.info("<< 키워드별 API 호출 완료 >>");
                        } else {
                            log.error("<< 키워드별 API 호출 중단: 실패 감지 >>");
                        }
                    });

            // 키워드 API 호출까지 성공 시, 최종 저장 건수 & 실행 시간 로그 남기기
            return keywordFailFast.thenAccept(keywordSavedCount -> {
                long end = System.currentTimeMillis();
                int totalSavedCount = categorySavedCount + keywordSavedCount;
//                log.info("📌(초기 세팅) API 호출 및 데이터 저장 완료\n" +
//                                "  - 총 처리 시간: {}ms\n" +
//                                "  - 총 데이터 수: {}건 (카테고리 API: {}건, 키워드 API: {}건)",
//                        (end - start), totalSavedCount, categorySavedCount, keywordSavedCount);
            });
        });
    }

    // 이미 저장된 웰니스 정보에 대해, 썸네일 이미지 S3에 업로드 후 컬럼(thumbnailUrl) 업데이트
    private void uploadThumbnailImgToS3() {
//        log.info("<< 썸네일 이미지 S3에 업로드 시작 >>");
        long start = System.currentTimeMillis();

        List<WellnessInfo> hasThumbnailUrlWellnessInfoList = wellnessInfoRepository.findByOriginalThumbnailUrlNotNull();

        List<CompletableFuture<Void>> futures = hasThumbnailUrlWellnessInfoList.stream()
                .map(w -> CompletableFuture.runAsync(() -> {
                    //log.info("[비동기 시작] contentId={}, thread={}", w.getContentId(), Thread.currentThread().getName());
                    String s3Url = processThumbnailImg(w.getContentId(), w.getOriginalThumbnailUrl());
                    if (s3Url != null && !s3Url.isBlank()) {
                        w.updateThumbnailUrl(s3Url);
                    }
                    //log.info("[비동기 완료] contentId={}, thread={}", w.getContentId(), Thread.currentThread().getName());
                }, asyncExecutor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        updateThumbnailUrl(hasThumbnailUrlWellnessInfoList);

        long end = System.currentTimeMillis();
//        log.info("<< 썸네일 이미지 S3에 업로드 완료 >>");

//        log.info("** 썸네일 이미지 S3에 업로드 완료 ** \n" +
//                "  - 총 처리 시간: {}ms",
//                end - start);
    }

    @Transactional
    void updateThumbnailUrl(List<WellnessInfo> wellnessInfoList){
        wellnessInfoRepository.saveAll(wellnessInfoList);
    }

    public String processThumbnailImg(String contentId, String originalUrl){
        if (originalUrl != null && !originalUrl.trim().isEmpty()) {
            return s3Service.uploadImgFromUrl(originalUrl, contentId);
        }
        return null;
    }

}

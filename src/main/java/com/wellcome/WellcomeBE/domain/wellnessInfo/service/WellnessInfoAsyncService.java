package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoAsyncService {

    private final WellnessInfoService2 wellnessInfoService2;

    // 한국관광공사 API 호출 및 데이터 저장 로직 실행
    public void fetchAllData() {
        log.info("[메인 스레드 확인] thread={}", Thread.currentThread().getName());

        long start = System.currentTimeMillis();

        // 카테고리별 API 호출
        log.info("<< 카테고리별 API 호출 시작 >>");
        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        for (CategoryDetail category : CategoryDetail.CATEGORY_PARAM_LIST) {
            futures.add(wellnessInfoService2.fetchAndSaveByCategory(category));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); //모든 카테고리 작업 완료 대기
        log.info("<< 카테고리별 API 호출 완료 >>");

        int categorySavedCount = futures.stream()
                .mapToInt(future -> {
                    try {
                        return future.get();
                    } catch (Exception e){
                        return 0;
                    }
                })
                .sum();

        // 키워드별 API 호출
        log.info("<< 키워드별 API 호출 시작 >>");
        int keywordSavedCount = wellnessInfoService2.fetchAndSaveByKeywords();
        log.info("<< 키워드별 API 호출 완료 >>");

        // 썸네일 이미지 URL -> S3에 업로드 후 thumbnailUrl 컬럼 업데이트
        log.info("<< 썸네일 이미지 S3에 업로드 시작 >>");
        wellnessInfoService2.uploadThumbnailImgToS3();
        log.info("<< 썸네일 이미지 S3에 업로드 완료 >>");

        long end = System.currentTimeMillis();
        int totalSavedCount = categorySavedCount + keywordSavedCount;
        log.info("** API 호출 및 데이터 저장 완료 ** \n" +
                "  - 총 처리 시간: {}ms \n" +
                "  - 총 데이터 수: {}건 (카테고리 API: {}건, 키워드 API: {}건)",
                (end - start), totalSavedCount, categorySavedCount, keywordSavedCount);
    }
}

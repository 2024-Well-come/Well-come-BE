package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.image.S3Service;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS;

@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoService2 {

    @Qualifier("categoryApiFetchService")
    private final WellnessInfoFetchTemplate<CategoryDetail> categoryApiFetchService;
    @Qualifier("keywordApiFetchService")
    private final WellnessInfoFetchTemplate<String> keywordApiFetchService;

    private final WellnessInfoRepository wellnessInfoRepository;
    private final S3Service s3Service;

    // 한국관광공사 API 호출 및 데이터 저장 로직 실행
    public void fetchAllData() {
        log.info("[메인 스레드 확인] thread={}", Thread.currentThread().getName());

        long start = System.currentTimeMillis();

        // 카테고리별 API 호출
        log.info("<< 카테고리별 API 호출 시작 >>");
        List<CompletableFuture<Integer>> categoryFutures = new ArrayList<>();
        for (CategoryDetail category : CategoryDetail.CATEGORY_PARAM_LIST) {
            categoryFutures.add(categoryApiFetchService.fetchAndSave(category));
        }
        CompletableFuture.allOf(categoryFutures.toArray(new CompletableFuture[0])).join(); //모든 카테고리 작업 완료 대기
        log.info("<< 카테고리별 API 호출 완료 >>");

        int categorySavedCount = categoryFutures.stream()
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
        List<CompletableFuture<Integer>> keywordFutures = new ArrayList<>();
        for(String keyword : KEYWORDS){
            keywordFutures.add(keywordApiFetchService.fetchAndSave(keyword));
        }
        CompletableFuture.allOf(keywordFutures.toArray(new CompletableFuture[0])).join(); //모든 키워드 작업 완료 대기
        log.info("<< 키워드별 API 호출 완료 >>");

        int keywordSavedCount = keywordFutures.stream()
                .mapToInt(future -> {
                    try {
                        return future.get();
                    } catch (Exception e){
                        return 0;
                    }
                })
                .sum();

        // 썸네일 이미지 URL -> S3에 업로드 후 thumbnailUrl 컬럼 업데이트
        log.info("<< 썸네일 이미지 S3에 업로드 시작 >>");
        uploadThumbnailImgToS3();
        log.info("<< 썸네일 이미지 S3에 업로드 완료 >>");

        long end = System.currentTimeMillis();
        int totalSavedCount = categorySavedCount + keywordSavedCount;
        log.info("** API 호출 및 데이터 저장 완료 ** \n" +
                "  - 총 처리 시간: {}ms \n" +
                "  - 총 데이터 수: {}건 (카테고리 API: {}건, 키워드 API: {}건)",
                (end - start), totalSavedCount, categorySavedCount, keywordSavedCount);
    }

    // 이미 저장된 웰니스 정보에 대해, 썸네일 이미지 S3에 업로드 후 컬럼(thumbnailUrl) 업데이트
    @Transactional
    public void uploadThumbnailImgToS3(){
        List<WellnessInfo> hasThumbnailUrlWellnessInfoList = wellnessInfoRepository.findByOriginalThumbnailUrlNotNull();

        hasThumbnailUrlWellnessInfoList.forEach(wellnessInfo -> {
            String originalUrl = wellnessInfo.getOriginalThumbnailUrl();

            if(originalUrl != null && !originalUrl.trim().isEmpty()){
                String s3Url = s3Service.uploadImgFromUrl(originalUrl, wellnessInfo.getContentId());
                wellnessInfo.updateThumbnailUrl(s3Url);
            }
        });
    }

}

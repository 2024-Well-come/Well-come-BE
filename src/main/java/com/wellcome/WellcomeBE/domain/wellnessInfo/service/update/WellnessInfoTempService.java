package com.wellcome.WellcomeBE.domain.wellnessInfo.service.update;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfoTemp;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoTempRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.category.CategoryApiTempService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.keyword.KeywordApiTempService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.setup.WellnessInfoService2;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS_TEST;

/**
 * 한국관광공사 데이터 업데이트
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoTempService {

    @Qualifier("categoryApiTempService")
    private final CategoryApiTempService categoryApiTempService;
    @Qualifier("keywordApiTempService")
    private final KeywordApiTempService keywordApiTempService;

    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoTempRepository tempWellnessInfoRepository;
    private final WellnessInfoService2 wellnessInfoService2;

    @Transactional
    public void updateTourInfo(){
        log.info("❗ WellnessInfo 데이터 업데이트 시작");

        // 새로운 API 호출 및 데이터 저장
        fetchAllNewData();

        // 기존 DB의 contentId와 새로운 DB의 contentId 비교하여 추가된/삭제된 데이터 찾기
        List<String> contentIds = wellnessInfoRepository.findAllContentId();
        List<String> newContentIds = tempWellnessInfoRepository.findAllContentId();

        // i) 새로 추가된 데이터가 있을 경우
        // - 새로운 WellnessInfoTemp 정보로 WellnessInfo 데이터 생성
        // - 썸네일 이미지 S3 업로드, 상세 사진 불러오기, parentId 값 채우기
        Set<String> addedIds = new HashSet<>(newContentIds);
        addedIds.removeAll(contentIds);
        System.out.println("✔ addedIds: " + addedIds.size() + "건");
        if(!addedIds.isEmpty()){
            processAddedContentIds(addedIds);
        }

        // ii) 삭제된 데이터가 있을 경우
        Set<String> deletedIds = new HashSet<>(contentIds);
        deletedIds.removeAll(newContentIds);
        System.out.println("✔ deletedIds: " + deletedIds.size() + "건");
        if(!deletedIds.isEmpty()){
            processDeletedContentIds(deletedIds);
        }

        log.info("❗ WellnessInfo 데이터 업데이트 종료");
    }

    // 새롭게 한국관광공사 API 호출한 후, WellnessInfoTemp 테이블에 임시 저장
    private void fetchAllNewData(){
        log.info("[메인 스레드 확인] thread={}", Thread.currentThread().getName());

        long start = System.currentTimeMillis();

        // WellnessInfoTemp 초기화
        tempWellnessInfoRepository.truncateTable();

        // 카테고리별 API 호출
        log.info("<< 카테고리별 API 호출 시작 >>");
        List<CompletableFuture<Integer>> categoryFutures = new ArrayList<>();
        for (CategoryDetail category : CategoryDetail.CATEGORY_PARAM_LIST_TEST) {
            categoryFutures.add(categoryApiTempService.fetchAndSave(category));
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
        for(String keyword : KEYWORDS_TEST){
            keywordFutures.add(keywordApiTempService.fetchAndSave(keyword));
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

        long end = System.currentTimeMillis();
        int totalSavedCount = categorySavedCount + keywordSavedCount;
        log.info("** API 호출 및 데이터 저장 완료 ** \n" +
                        "  - 총 처리 시간: {}ms \n" +
                        "  - 총 데이터 수: {}건 (카테고리 API: {}건, 키워드 API: {}건)",
                (end - start), totalSavedCount, categorySavedCount, keywordSavedCount);
    }

    // 추가된 데이터 처리
    private void processAddedContentIds(Set<String> addedIds){
        
        // addedIds에 대한 WellnessInfoTemp 데이터 가져오기
        List<WellnessInfoTemp> wellnessInfoTemps = tempWellnessInfoRepository.findByContentIdIn(addedIds);
        Map<String, WellnessInfoTemp> newData = wellnessInfoTemps.stream()
                .collect(Collectors.toMap(
                        WellnessInfoTemp::getContentId,  // key: contentId
                        Function.identity(),             // value: WellnessInfoTemp
                        (existing, duplicate) -> duplicate
                ));

        // 썸네일 이미지 S3에 업로드
        Map<String, String> thumbnailUrls = wellnessInfoService2.uploadThumbnailImgToS3ByContentId(newData);

        // 상세 사진 저장
        // todo 업데이트 필요

        // parentId 값 채우기
        // todo 임시로 parentId=null로 설정, 업데이트 필요

        // 새로운 WellnessInfo 데이터 생성
        List<WellnessInfo> wellnessInfos = newData.entrySet().stream()
                .map(entry -> {
                    String contentId = entry.getKey();
                    WellnessInfoTemp temp = entry.getValue();
                    String thumbnailUrl = thumbnailUrls.get(contentId);
                    //String parentId = parentIds.get(contentId);
                    //return WellnessInfo.of(temp, thumbnailUrl, parentId);
                    try {
                        return WellnessInfo.of(temp, thumbnailUrl, null);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("➕ 저장 예정 데이터: {}건", wellnessInfos.size());
        wellnessInfoRepository.saveAll(wellnessInfos);
    }

    // 삭제된 데이터 처리
    private void processDeletedContentIds(Set<String> deletedIds){

        // deletedIds의 WellnessInfo isDeleted 컬럼 true로 업데이트
        wellnessInfoRepository.bulkUpdateIsDeleted(deletedIds);

        /**
         * 추가 작업
         * - WellnessInfo isDeleted 컬럼 추가
         * - 웰니스 목록 조회에서는 isDeleted = false인 값들 중 조회되도록 수정 (todo querydsl 코드에 추가 반영 필요)
         * - 웰니스 정보 상세 조회 시 isDelete = true일 경우, opendetail에서 영업종료 표시 대신 '폐점됨' 으로 보이도록 수정
         */
    }
}

package com.wellcome.WellcomeBE.domain.wellnessInfo.service.update;

import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService2;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfoTemp;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal.WellnessInfoTempData;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoTempRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.category.CategoryApiTempService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.keyword.KeywordApiTempService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.setup.WellnessInfoService2;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.service.WellnessInfoImgService3;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
    private final Executor asyncExecutor;
    private final GoogleMapInfoService2 googleMapInfoService2;
    private final WellnessInfoImgService3 wellnessInfoImgService3;

    @Transactional
    public void updateTourInfo(){
        log.info("❗ WellnessInfo 데이터 업데이트 시작");

        // 새로운 API 호출 및 데이터 저장
        fetchAllNewData();

        // 기존 DB의 contentId와 새로운 DB의 contentId 비교하여 추가된/삭제된 데이터 찾기
        List<String> contentIds = wellnessInfoRepository.findAllContentId();
        List<String> newContentIds = tempWellnessInfoRepository.findAllContentId();

        // i) 새로 추가된 데이터가 있을 경우
        // - 썸네일 이미지 S3 업로드, parentId 값 채우기 (preparedData)
        // - preparedData + WellnessInfoTemp -> WellnessInfo 데이터 생성
        // - 상세사진 불러와서 저장
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

        // 썸네일 이미지 S3에 업로드 및 parentId 값 찾기
        List<WellnessInfoTempData> preparedData = prepareWellnessInfoData(newData);

        // 새로운 WellnessInfo 데이터 생성 및 저장
        saveAddedWellnessInfos(preparedData, newData);

        // 상세사진 저장
        addedIds.forEach(contentId -> wellnessInfoImgService3.singleFetchAndSaveTourImages(contentId));
    }

    // 썸네일 이미지 S3 업로드 및 parentId 값 찾기
    private List<WellnessInfoTempData> prepareWellnessInfoData(Map<String, WellnessInfoTemp> newData){
        List<CompletableFuture<WellnessInfoTempData>> futures = newData.entrySet().stream()
                .map(entry -> {
                    String contentId = entry.getKey();
                    WellnessInfoTemp temp = entry.getValue();

                    // 썸네일 이미지 업로드
                    CompletableFuture<String> thumbnailUrlFuture = CompletableFuture.supplyAsync(
                            () -> wellnessInfoService2.processThumbnailImg(contentId, temp.getOriginalThumbnailUrl()),
                            asyncExecutor);

                    // parentId 값 찾기
                    CompletableFuture<String> parentIdFuture = CompletableFuture.completedFuture(null); //todo parentId 처리 로직 추가 필요
                    //CompletableFuture<String> parentIdFuture = googleMapInfoService2.getParentIdByGoogleMap(temp.getAddress(), temp.getTitle());

                    // 두 비동기 작업 병합
                    return thumbnailUrlFuture.thenCombine(parentIdFuture,
                            (thumbnailUrl, parentId) -> {
                                //log.info("[모두 완료] contentId={}, thread={}", contentId, Thread.currentThread().getName());
                                return new WellnessInfoTempData(contentId, thumbnailUrl, parentId);
                            });
                })
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Transactional
    private void saveAddedWellnessInfos(List<WellnessInfoTempData> preparedData, Map<String, WellnessInfoTemp> newData){
        List<WellnessInfo> wellnessInfos = preparedData.stream()
                .map(data -> {
                    WellnessInfoTemp temp = newData.get(data.getContentId());
                    try {
                        return WellnessInfo.of(temp, data.getThumbnailUrl(), data.getParentId());
                    } catch (ParseException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        wellnessInfoRepository.saveAll(wellnessInfos);
    }

    // 삭제된 데이터 처리
    @Transactional
    private void processDeletedContentIds(Set<String> deletedIds){

        // deletedIds의 WellnessInfo isDeleted 컬럼 true로 업데이트
        wellnessInfoRepository.bulkUpdateIsDeleted(deletedIds);
    }
}

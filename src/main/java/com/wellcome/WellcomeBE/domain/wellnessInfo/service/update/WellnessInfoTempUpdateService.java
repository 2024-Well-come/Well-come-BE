package com.wellcome.WellcomeBE.domain.wellnessInfo.service.update;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfoTemp;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal.WellnessInfoTempData;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoTempRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.io.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WellnessInfoTempUpdateService {
    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoTempRepository tempWellnessInfoRepository;

    // WellnessInfoTemp 테이블 초기화
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void truncateWellnessInfoTemp(){
        tempWellnessInfoRepository.truncateTable();
    }

    // 추가된 데이터 저장
    @Transactional
    public void saveAddedWellnessInfos(List<WellnessInfoTempData> preparedData, Map<String, WellnessInfoTemp> newData){
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

    // 삭제된 데이터 컬럼 상태값 변경
    @Transactional
    public void processDeletedContentIds(Set<String> deletedIds){
        // deletedIds의 WellnessInfo isDeleted 컬럼 true로 업데이트
        wellnessInfoRepository.bulkUpdateIsDeleted(deletedIds);
    }
}


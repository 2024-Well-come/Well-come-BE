package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class WellnessInfoApiService {

    private final WellnessInfoRepository wellnessInfoRepository;

    /**
     * 웰니스 장소 추천 목록 조회
     */
    public WellnessInfoResponse getWellnessInfoList(int page, WellnessInfoListRequest request) {

        // TODO 로그인 여부 확인 (develop에 회원가입 & 로그인 기능 반영 후 추가 구현 예정)
        boolean isLogin = false;

        // 웰니스 정보 조회
        PageRequest pageRequest = PageRequest.of(page, 5);

        List<Thema> themaList = request.getThemaList();
        List<Sigungu> sigunguList = request.getSigunguList();

        // TODO 로그인 한 유저의 경우, 좋아요 내역도 함께 조회
        if(isLogin) {
            //Page<Object[]> result = wellnessInfoRepository.findByThemaAndSigungu(pageRequest, member, themaList, sigunguList);
        }else {
            //Page<WellnessInfo> result = wellnessInfoRepository.findByThemaAndSigunguWithoutLikes(pageRequest, themaList, sigunguList);
        }

        Page<WellnessInfo> result = wellnessInfoRepository.findByThemaAndSigunguWithoutLikes(pageRequest, themaList, sigunguList);

        List<WellnessInfoResponse.WellnessInfoList.WellnessInfoItem> wellnessInfoItemList = result.stream()
                .map(wellnessInfo -> WellnessInfoResponse.WellnessInfoList.WellnessInfoItem.from(wellnessInfo, false))
                .collect(Collectors.toList());
        WellnessInfoResponse.WellnessInfoList WellnessInfoList = WellnessInfoResponse.WellnessInfoList.from(
                result.getTotalElements(), result.getNumber(),
                result.hasPrevious(), result.hasNext(),
                wellnessInfoItemList);

        return WellnessInfoResponse.from(themaList, sigunguList, request, WellnessInfoList);

    }
}

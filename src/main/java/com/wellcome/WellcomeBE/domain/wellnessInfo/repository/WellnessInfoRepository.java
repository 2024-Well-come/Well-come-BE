package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long>, WellnessInfoRepositoryCustom{

    List<WellnessInfo> findByOriginalThumbnailUrlNotNull();

    @Query("SELECT new com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly(w.contentId) FROM WellnessInfo w")
    List<WellnessContentIdOnly> findAllContentIdOnly();
    Optional<WellnessInfo> findByContentId(String contentId);

    //TODO 병렬 스레드 이후 삭제할 로직입니다.

}

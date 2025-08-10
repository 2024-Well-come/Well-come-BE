package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long>, WellnessInfoRepositoryCustom{

    List<WellnessInfo> findByOriginalThumbnailUrlNotNull();

    //TODO 병렬 스레드 이후 삭제할 로직입니다.
    Page<WellnessInfo> findAll(Pageable pageable);
}

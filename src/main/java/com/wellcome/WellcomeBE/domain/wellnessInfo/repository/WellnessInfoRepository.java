package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long>, WellnessInfoRepositoryCustom{

    List<WellnessInfo> findByOriginalThumbnailUrlNotNull();

    @Query("SELECT new com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly(w.contentId) FROM WellnessInfo w")
    List<WellnessContentIdOnly> findAllContentIdOnly();
    Optional<WellnessInfo> findByContentId(String contentId);

    List<WellnessInfo> findByThumbnailUrlNotNull();

    //TODO 병렬 스레드 이후 삭제할 로직입니다.
    Page<WellnessInfo> findAll(Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE WellnessInfo w SET w.parentId = :parentId WHERE w.id = :id")
    int updateParentIdById(@Param("id") Long id, @Param("parentId") String parentId);

    @Modifying
    @Query("UPDATE WellnessInfo w SET w.view = w.view + 1 WHERE w.id = :id")
    void incrementView(@Param("id") Long id);

}

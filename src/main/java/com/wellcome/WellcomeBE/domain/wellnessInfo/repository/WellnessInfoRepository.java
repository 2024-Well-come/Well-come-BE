package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long>, WellnessInfoRepositoryCustom{

    List<WellnessInfo> findByOriginalThumbnailUrlNotNull();

    List<WellnessInfo> findByThumbnailUrlNotNull();

    //TODO 병렬 스레드 이후 삭제할 로직입니다.
    Page<WellnessInfo> findAll(Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE WellnessInfo w SET w.parentId = :parentId WHERE w.id = :id")
    int updateParentIdById(@Param("id") Long id, @Param("parentId") String parentId);

    @Query("SELECT w.contentId FROM WellnessInfo w")
    List<String> findAllContentId();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE WellnessInfo w " +
            "SET w.isDeleted = true " +
            "WHERE w.contentId IN :contentIds")
    int bulkUpdateIsDeleted(@Param("contentIds") Set<String> contentIds);

    @Query("SELECT new com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly(w.contentId) FROM WellnessInfo w")
    List<WellnessContentIdOnly> findAllContentIdOnly();
    Optional<WellnessInfo> findByContentId(String contentId);

    List<WellnessInfo> findByContentIdIn(Set<String> contentIds);
}

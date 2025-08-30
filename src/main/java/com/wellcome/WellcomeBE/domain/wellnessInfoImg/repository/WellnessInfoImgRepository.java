package com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WellnessInfoImgRepository extends JpaRepository<WellnessInfoImg,Long> {
    @Query("SELECT w.imgUrl FROM WellnessInfoImg w WHERE w.wellnessInfo = :wellnessInfo")
    List<String> findByWellnessInfo(WellnessInfo wellnessInfo);

    @Modifying
    @Transactional
    @Query(
            value = "INSERT IGNORE INTO wellness_info_img " +
                    "(contentid, img_name, img_url, wellness_info_id, created_at, updated_at) " +
                    "VALUES (:contentId, :imgName, :imgUrl, :wellnessInfoId, NOW(), NOW())",
            nativeQuery = true
    )
    int insertIgnore(
            @Param("contentId") String contentId,
            @Param("imgName") String imgName,
            @Param("imgUrl") String imgUrl,
            @Param("wellnessInfoId") Long wellnessInfoId
    );

    @Query("select i.imgUrl from WellnessInfoImg i where i.wellnessInfo.contentId = :contentId")
    List<String> findImgUrlsByContentId(@Param("contentId") String contentId);

}
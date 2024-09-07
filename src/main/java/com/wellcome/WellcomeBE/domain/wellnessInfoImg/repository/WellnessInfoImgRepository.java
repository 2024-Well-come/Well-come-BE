package com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WellnessInfoImgRepository extends JpaRepository<WellnessInfoImg,Long> {
    @Query("SELECT w.imgUrl FROM WellnessInfoImg w WHERE w.wellnessInfo = :wellnessInfo")
    List<String> findByWellnessInfo(WellnessInfo wellnessInfo);
}

package com.wellcome.WellcomeBE.domain.wellnessInfoImg;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WellnessInfoImgRepository  extends JpaRepository<WellnessInfoImg,Long> {

    @Query("SELECT w.imgUrl FROM WellnessInfoImg w WHERE w.wellnessInfo = :wellnessInfo")
    List<String> findByWellnessInfo(WellnessInfo wellnessInfo);
}

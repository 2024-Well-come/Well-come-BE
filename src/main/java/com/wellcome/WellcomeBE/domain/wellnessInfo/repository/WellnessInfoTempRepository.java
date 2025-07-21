package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfoTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface WellnessInfoTempRepository extends JpaRepository<WellnessInfoTemp, Long> {

    @Query("SELECT wt.contentId FROM WellnessInfoTemp wt")
    List<String> findAllContentId();

    List<WellnessInfoTemp> findByContentIdIn(Set<String> contentIds);

    @Modifying
    @Query(value = "TRUNCATE TABLE wellness_info_temp", nativeQuery = true)
    void truncateTable();
}

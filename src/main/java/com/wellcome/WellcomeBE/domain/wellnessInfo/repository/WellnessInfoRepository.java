package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WellnessInfoRepository extends JpaRepository<WellnessInfo, Long> {
}

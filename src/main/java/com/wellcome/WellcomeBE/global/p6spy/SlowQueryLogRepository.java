package com.wellcome.WellcomeBE.global.p6spy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlowQueryLogRepository extends JpaRepository<SlowQueryLog, Long> {
}

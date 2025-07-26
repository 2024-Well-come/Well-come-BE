package com.wellcome.WellcomeBE;

import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.NativeSqlCreator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class NativeSqlCreatorTestConfig {
    @Bean
    public NativeSqlCreator nativeSqlCreator() {
        return new NativeSqlCreator();
    }
}
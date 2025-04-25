package com.wellcome.WellcomeBE.global.p6spy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P6SpyConfig {

    @Bean
    public P6SpyEventListener p6SpyCustomEventListener() {
        return new P6SpyEventListener();
    }

    @Bean
    public CustomP6SpyLogger p6SpyCustomFormatter() {
        return new CustomP6SpyLogger();
    }
}
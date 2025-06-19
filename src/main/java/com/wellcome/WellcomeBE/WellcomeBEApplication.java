package com.wellcome.WellcomeBE;

import com.wellcome.WellcomeBE.global.p6spy.QueryLogProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableConfigurationProperties(QueryLogProperties.class)
public class WellcomeBEApplication {

	public static void main(String[] args) {
		SpringApplication.run(WellcomeBEApplication.class, args);
	}

}

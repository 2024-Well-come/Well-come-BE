package com.wellcome.WellcomeBE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WellcomeBEApplication {

	public static void main(String[] args) {
		SpringApplication.run(WellcomeBEApplication.class, args);
	}

}

package com.buildscheduler.buildscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BuildschedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuildschedulerApplication.class, args);
	}
}

package com.developerChallenge.developer_Challenge_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DeveloperChallengeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeveloperChallengeBackendApplication.class, args);
	}

}

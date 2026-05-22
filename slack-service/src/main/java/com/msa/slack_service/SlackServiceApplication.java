package com.msa.slack_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "com.msa")
@EnableScheduling
public class SlackServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(SlackServiceApplication.class, args);
	}

}

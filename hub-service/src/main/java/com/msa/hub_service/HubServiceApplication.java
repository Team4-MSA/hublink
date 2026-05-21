package com.msa.hub_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.msa"})
@EnableScheduling
@EnableDiscoveryClient
public class HubServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubServiceApplication.class, args);
	}

}

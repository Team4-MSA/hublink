package com.msa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuthApplication {
	public static void main(String[] args) {
		String name = null;
		System.out.println(name.length());
		SpringApplication.run(AuthApplication.class, args);
	}

}

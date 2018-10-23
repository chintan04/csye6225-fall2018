package com.csye6225;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Runner {

	public static void main(String[] args) {
		System.getProperties().put("spring.profiles.active","dev");
		SpringApplication.run(Runner.class, args);
	}
}

package com.csye6225;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class Runner extends SpringBootServletInitializer {

	public static void main(String[] args) {
		
		SpringApplication.run(Runner.class, args);
	}
}

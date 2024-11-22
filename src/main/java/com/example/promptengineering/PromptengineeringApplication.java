package com.example.promptengineering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class PromptengineeringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PromptengineeringApplication.class, args);
	}

}

package com.ureka.techpost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TechpostApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechpostApplication.class, args);
	}

}

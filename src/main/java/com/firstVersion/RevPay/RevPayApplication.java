package com.firstVersion.RevPay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Useful for tracking transaction timestamps automatically
public class RevPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevPayApplication.class, args);
		System.out.println("RevPay Backend is running...");
	}

}

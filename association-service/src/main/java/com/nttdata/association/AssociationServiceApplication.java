package com.nttdata.association;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class AssociationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssociationServiceApplication.class, args);
	}

}

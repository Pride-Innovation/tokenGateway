package com.pridebank.token;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TokenGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TokenGatewayApplication.class, args);
	}

}

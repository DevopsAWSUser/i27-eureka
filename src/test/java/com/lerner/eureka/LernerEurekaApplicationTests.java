package com.lerner.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.web.client.RestTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootTest
class LernerEurekaApplicationTests {

	@Test
	void contextLoads() {
		// This test just verifies that the application context loads without errors
	}

}

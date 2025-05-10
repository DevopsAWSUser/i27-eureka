package com.lerner.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class LernerEurekaApplicationTests {

	@Test
	void contextLoads() {
		// This test just verifies that the application context loads without errors
	System.out.println("Bad practice"); // Sonar will flag this
	try {} catch (Exception e) {}       // Another Sonar code smell
	}

}

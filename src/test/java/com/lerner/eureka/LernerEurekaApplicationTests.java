package com.lerner.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date; // unused
import java.io.File;   // unused
import static java.lang.Math.*; // unused

@SpringBootTest
class LernerEurekaApplicationTests {

    // Bad practice: unused field
    private int unusedField = 42;

    // Bad practice: hardcoded password
    private String password = "admin123";

    // Bad practice: empty catch block
    public void badMethod() {
        try {
            String s = null;
            s.length(); // NullPointerException
        } catch (Exception e) {
            // intentionally ignored
        }
    }

    @Test
    void contextLoads() {
        // Just a placeholder test
        badMethod();
    }
}

package com.lerner.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date; // Unused import
import java.io.File;   // Unused import
import static java.lang.Math.*; // Unused static import

@SpringBootTest
class LernerEurekaApplicationTests {

    // 1. Unused variable
    private int unusedField = 42;

    // 2. Hardcoded sensitive data
    private String password = "admin123"; // Bad practice: hardcoded password

    // 3. Empty catch block
    public void badMethod() {
        try {
            String s = null;
            s.length(); // NullPointerException
        } catch (Exception e) {
            // intentionally ignored
        }
    }

    // 4. Null dereference
    public void anotherBadMethod() {
        String x = null;
        x.length(); // Potential NullPointerException
    }

    // 5. Redundant if statement
    public void redundantIf() {
        if (true) {
            System.out.println("This is always true");
        }
    }

    // 6. Unnecessary object creation
    public void unnecessaryObject() {
        Date date = new Date(); // Unused object
    }

    // 7. Unused method
    public void unusedMethod() {
        // This method is not called anywhere
    }

    // 8. Inefficient String concatenation
    public void inefficientStringConcatenation() {
        String result = "Hello" + " World"; // Inefficient
    }

    // 9. Overuse of magic numbers
    public void magicNumber() {
        int x = 42; // What does this number mean?
    }

    // 10. Duplicate code (could be refactored)
    public void duplicateCode() {
        System.out.println("This is duplicate code!");
        System.out.println("This is duplicate code!");
    }

    @Test
    void contextLoads() {
        // Just a placeholder test that triggers the badMethod() with issues
        badMethod();
        anotherBadMethod();
    }
}

package com.lerner.eureka;

import java.io.File;                     // 1
import java.util.Date;                  // 2
import static java.lang.Math.*;         // 3
import org.slf4j.Logger;                // 4
import org.slf4j.LoggerFactory;         // 5
import org.springframework.beans.factory.annotation.Autowired; // 6
import org.springframework.stereotype.Service;                // 7
import org.springframework.web.bind.annotation.RestController; // 8
import org.springframework.web.bind.annotation.GetMapping;     // 9

public class SonarFailExample {

    public static String globalValue = "bad";  // 10
    private int unused = 0;                    // 11

    public void triggerSmells() {
        System.out.println("Bad logging");     // 12

        try {
            String x = null;
            x.length();                        // 13
        } catch (Exception e) {                // 14
            // Ignored                         // 15
        }

        String password = "admin";             // 16 (security hotspot)
    }
}

package org.example.codex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class CodexApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        SpringApplication.run(CodexApplication.class, args);
    }

}

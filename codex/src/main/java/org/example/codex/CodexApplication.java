package org.example.codex;

import org.example.codex.controller.SystemController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class CodexApplication {
    public static void main(String[] args) throws InterruptedException {
        //wait for database startup
//        TimeUnit.SECONDS.sleep(5);

        SpringApplication.run(CodexApplication.class, args);
    }

}

package com.notoai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NotoAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotoAiApplication.class, args);
    }
}

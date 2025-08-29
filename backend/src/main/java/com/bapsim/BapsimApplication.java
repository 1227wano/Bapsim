package com.bapsim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class BapsimApplication {

    public static void main(String[] args) {
        SpringApplication.run(BapsimApplication.class, args);
    }
}

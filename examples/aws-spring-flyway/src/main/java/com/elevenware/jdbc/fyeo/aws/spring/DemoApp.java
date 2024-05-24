package com.elevenware.jdbc.fyeo.aws.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.elevenware.jdbc.fyeo.aws.spring")
public class DemoApp {

    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class);
    }

}

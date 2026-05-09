package com.teamone.peacelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PeaceLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeaceLinkApplication.class, args);
    }

}

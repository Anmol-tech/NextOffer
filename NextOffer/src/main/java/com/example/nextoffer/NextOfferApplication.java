package com.example.nextoffer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NextOfferApplication {

    public static void main(String[] args) {
        SpringApplication.run(NextOfferApplication.class, args);
    }

}

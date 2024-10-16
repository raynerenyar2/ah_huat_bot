package org.telegram.toto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.toto.repository.ChatRepo;
import org.telegram.toto.repository.entities.Chat;
import org.telegram.toto.service.SubscriberService;

@SpringBootApplication(scanBasePackages = {
        "org.telegram.toto.service",
        "org.telegram.toto.repository",
        "org.telegram.toto.controller",
        "org.telegram.toto.config" })
@EntityScan("org.telegram.toto.repository.entities")
@EnableScheduling
public class TotoApplication implements CommandLineRunner {

    @Autowired
    private SubscriberService service;

    public static void main(String[] args) {
        SpringApplication.run(TotoApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        // service.something();
    }

}

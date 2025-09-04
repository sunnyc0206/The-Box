package com.thebox;

import com.thebox.service.TheBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TheBoxApplication implements CommandLineRunner {
    
    @Autowired
    private TheBoxService iptvService;

    public static void main(String[] args) {
        SpringApplication.run(TheBoxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting TheBox application...");
        System.out.println("Fetching channels...");
        iptvService.fetchAndUpdateChannels();
        System.out.println("TheBox application started successfully!");
        System.out.println("API available at: http://localhost:8080/api");
        System.out.println("H2 Console at: http://localhost:8080/h2-console");
    }
} 
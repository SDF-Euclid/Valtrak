package com.example.valtrak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The game server (REST API + database). Run this first; the desktop client
 * ({@link ValtrakClient}) connects to it.
 */
@SpringBootApplication
public class ValtrakApplication {
    public static void main(String[] args) {
        SpringApplication.run(ValtrakApplication.class, args);
    }
}

package com.errorcab;

import com.errorcab.database.DatabaseManager;
import com.errorcab.database.DatabaseSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

/**
 * Spring Boot Application Entry Point for ERRORCab.
 * Team: ERROR
 * Tagline: "Book Smart. Ride Safe."
 */
@SpringBootApplication
public class ErrorCabApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ErrorCabApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize SQLite Database schema and seed demo data automatically
        DatabaseManager.getInstance();
        DatabaseSeeder.seedIfEmpty();

        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {}

        System.out.println("================================================================");
        System.out.println("  🚖 ERRORCab - Indian Cab Ride Booking & Tracking System       ");
        System.out.println("  Team: ERROR  |  Tagline: \"Book Smart. Ride Safe.\"            ");
        System.out.println("================================================================");
        System.out.println("  Local Desktop URL:  http://localhost:8080                     ");
        System.out.println("  Mobile/Wi-Fi URL:   http://" + localIp + ":8080               ");
        System.out.println("================================================================");
    }
}

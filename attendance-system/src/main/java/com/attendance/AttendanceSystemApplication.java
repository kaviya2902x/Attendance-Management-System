package com.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AttendanceSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
        System.out.println("=========================================");
        System.out.println("✅ ATTENDANCE SYSTEM STARTED SUCCESSFULLY!");
        System.out.println("✅ Access URL: http://localhost:8080");
        System.out.println("✅ Admin Login: username: admin, password: admin123");
        System.out.println("✅ Employee Login: username: john.doe, password: emp123");
        System.out.println("✅ Database: http://localhost/phpmyadmin");
        System.out.println("=========================================");
    }
}
package com.example.ziovpo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UsersRepository;

@SpringBootApplication
public class ZiovpoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZiovpoApplication.class, args);
    }

    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner commandLineRunner(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            String adminUsername = System.getenv("ADMIN_USERNAME");
            String adminPassword = System.getenv("ADMIN_PASSWORD");
            String adminEmail = System.getenv("ADMIN_EMAIL");

            if (adminUsername == null) adminUsername = "admin";
            if (adminPassword == null) adminPassword = "admin";
            if (adminEmail == null) adminEmail = "admin@test.com";

            if (usersRepository.findByUsername(adminUsername).isEmpty()) {
                Users admin = new Users();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setEmail(adminEmail);
                admin.setRole("ROLE_ADMIN");
                usersRepository.save(admin);
                System.out.println(" Администратор создан. ");
            }
        };
    }
}
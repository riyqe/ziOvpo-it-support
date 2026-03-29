package com.example.itsupp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.itsupp.model.Users;
import com.example.itsupp.repository.UsersRepository;

@SpringBootApplication
@EnableScheduling
public class itsuppApplication {

    public static void main(String[] args) {
        SpringApplication.run(itsuppApplication.class, args);
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
                admin.setPassword(passwordEncoder.encode(adminPassword)); //шифр
                admin.setEmail(adminEmail);
                admin.setRole("ROLE_ADMIN");
                admin.setDepartment("IT Department");
                usersRepository.save(admin);
                System.out.println(" Администратор создан. ");
            }
        };
    }
}

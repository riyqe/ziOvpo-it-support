package com.example.itsupp;

import com.example.itsupp.model.Users;
import com.example.itsupp.repository.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class itsuppApplication {

    public static void main(String[] args) {
        SpringApplication.run(itsuppApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // читаем из переменных окружения
            String adminUsername = System.getenv("ADMIN_USERNAME");
            String adminPassword = System.getenv("ADMIN_PASSWORD");
            String adminEmail = System.getenv("ADMIN_EMAIL");

            // если не задать — использует дефолты (для локального запуска)
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

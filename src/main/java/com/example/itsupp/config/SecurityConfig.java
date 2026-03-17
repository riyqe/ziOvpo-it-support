package com.example.itsupp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // для всех
                        .requestMatchers("/api/auth/**").permitAll()

                        // админское
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")

                        // админ может создавать категории
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/executors").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/sla").hasRole("ADMIN")

                        // админ может вручную эскалировать
                        .requestMatchers(HttpMethod.POST, "/api/tickets/escalate").hasRole("ADMIN")

                        // админ видит список просроченных
                        .requestMatchers(HttpMethod.GET, "/api/tickets/overdue").hasRole("ADMIN")

                        // админ может привязать SLA
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/sla/*").hasRole("ADMIN")

                        // обновление/удаление категорий, исполнителей, SLA
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/executors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/executors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/sla/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/sla/**").hasRole("ADMIN")

                        // все авторизованные
                        .requestMatchers(HttpMethod.GET, "/api/tickets/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/executors/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/sla/**").authenticated()

                        // создание и работа с тикетами для всех авторизованных
                        .requestMatchers(HttpMethod.POST, "/api/tickets").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/assign/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/resolve").authenticated()

                        // остальное запрещено
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

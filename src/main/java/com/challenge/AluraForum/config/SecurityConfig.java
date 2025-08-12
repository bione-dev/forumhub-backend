package com.challenge.AluraForum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // para REST sem sessão, desabilite CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/topicos").permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}

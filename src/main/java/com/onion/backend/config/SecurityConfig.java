package com.onion.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests((requests) -> requests
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/users/signUp","/api/users/login").permitAll()  // Swagger UI와 API 문서에 대한 접근 허용
            .anyRequest().authenticated()  // 나머지 요청은 인증된 사용자만 접근 가능
        );
//        .formLogin(Customizer.withDefaults());
//        .logout((logout) -> logout
//            .permitAll()
//        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
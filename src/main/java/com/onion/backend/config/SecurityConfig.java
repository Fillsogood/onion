package com.onion.backend.config;

import com.onion.backend.JWT.JwtAuthenticationFilter;
import com.onion.backend.JWT.JwtUtil;
import com.onion.backend.service.JwtBlacklistService;
import com.onion.backend.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private final UserDetailsServiceImpl userDetailsService;
  @Autowired
  private final JwtUtil jwtUtil;

  @Autowired
  private final JwtBlacklistService jwtBlacklistService;

  public SecurityConfig(UserDetailsServiceImpl userDetailsService ,JwtUtil jwtUtil, JwtBlacklistService jwtBlacklistService) {
    this.userDetailsService = userDetailsService;
    this.jwtUtil = jwtUtil;
    this.jwtBlacklistService = jwtBlacklistService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests((requests) -> requests
            .requestMatchers("/swagger-ui/**","/swagger-resources/**","/v3/api-docs/**", "/api/users/signUp", "/api/auth/login","/api/ads","/api/ads/**").permitAll()  // Swagger UI와 API 문서에 대한 접근 허용
            .anyRequest().authenticated()  // 나머지 요청은 인증된 사용자만 접근 가능
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService, jwtBlacklistService), UsernamePasswordAuthenticationFilter.class)  // 필터 등록
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);  // 세션 없음
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .userDetailsService(userDetailsService)  // 사용자 인증 정보를 서비스에서 가져오기
        .passwordEncoder(passwordEncoder())
        .and()
        .build();
  }

}
package com.onion.backend.JWT;

import com.onion.backend.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private JwtBlacklistService jwtBlacklistService;
  public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, JwtBlacklistService jwtBlacklistService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.jwtBlacklistService = jwtBlacklistService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    String token = extractJwtToken(request);
    if (token != null && !jwtBlacklistService.isTokenBlacklisted(token) && jwtUtil.validateJwtToken(token)) {
      String username = jwtUtil.getUsernameFromJwtToken(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }

  // JWT 토큰을 요청 헤더에서 추출하는 메서드
  private String extractJwtToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }

    if(header == null){
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for(Cookie cookie : cookies){
          if("token".equals(cookie.getName())){
            return cookie.getValue();
          }
        }
      }
    }

    return null;
  }
}

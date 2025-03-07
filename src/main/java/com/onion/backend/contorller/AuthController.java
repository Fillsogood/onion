package com.onion.backend.contorller;

import com.onion.backend.JWT.JwtUtil;


import com.onion.backend.dto.LoginUserDto;
import com.onion.backend.service.JwtBlacklistService;
import com.onion.backend.service.UserDetailsServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserDetailsServiceImpl userDetailsService;
  private final JwtBlacklistService jwtBlacklistService;

  public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, JwtBlacklistService jwtBlacklistService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.jwtBlacklistService = jwtBlacklistService;
  }

  // 로그인 API
  @PostMapping("/login")
  public ResponseEntity<String> Login(@Valid @RequestBody LoginUserDto loginUser, HttpServletResponse response) throws AuthenticationException {

    // 사용자 인증
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword())
    );
    UserDetails userDetails = userDetailsService.loadUserByUsername(loginUser.getUsername());
    String token = jwtUtil.generateJwtToken(userDetails.getUsername());

    Cookie cookie = new Cookie("token", token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(3600);

    response.addCookie(cookie);

    return ResponseEntity.ok(token);
  }

  @PostMapping("/logout")
  public void logout(HttpServletResponse response) {
    Cookie cookie = new Cookie("token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  @PostMapping("/logout/all")
  public void logout(@RequestParam(required = false) String requestToken,@CookieValue(value = "token", required = false) String cookietoken, HttpServletResponse response, HttpServletRequest request) {
//    String token = requestToken.isEmpty() ? requestToken : cookietoken;
    String token = null;
    String bearerToken = request.getHeader("Authorization");
    if (requestToken != null){
      token = requestToken;
    }else if(cookietoken != null){
      token = cookietoken;
    }else if(bearerToken != null && bearerToken.startsWith("Bearer ")){
      token = bearerToken.substring(7);
    }
    Date expiration = jwtUtil.getExpirationFromJwtToken(token);
    Instant instant = expiration.toInstant();
    LocalDateTime expiratAt =  instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    String username = jwtUtil.getUsernameFromJwtToken(token);
    jwtBlacklistService.addToBlacklist(token, expiratAt, username);
    Cookie cookie = new Cookie("token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  @PostMapping("/token/validation")
  @ResponseStatus(HttpStatus.OK)
  public void jwtTokenValidation(@RequestParam String token) {
    if (!jwtUtil.validateJwtToken(token)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "JWT Token is valid");
    }
  }
}

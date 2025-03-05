package com.onion.backend.Contorller;

import com.onion.backend.JWT.JwtUtil;
import com.onion.backend.dto.LoginUser;

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


@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserDetailsServiceImpl userDetailsService;

  public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  // 로그인 API
  @PostMapping("/login")
  public ResponseEntity<String> Login(@Valid @RequestBody LoginUser loginUser, HttpServletResponse response) throws AuthenticationException {

    // 사용자 인증
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword())
    );
    UserDetails userDetails = userDetailsService.loadUserByUsername(loginUser.getUsername());
    String token = jwtUtil.generateJwtToken(userDetails.getUsername());

    Cookie cookie = new Cookie("token", token);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(3600);

    response.addCookie(cookie);

    return ResponseEntity.ok(token);
  }

  @PostMapping
  public void logout(HttpServletResponse response){
    Cookie cookie = new Cookie("token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0); // 쿠키 삭제
    response.addCookie(cookie);
  }

  @PostMapping("/token/validation")
  @ResponseStatus(HttpStatus.OK)
  public void jwtTokenValidation(@RequestParam String token) {
    if (jwtUtil.validateJwtToken(token)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "JWT Token is valid");
    }
  }
}

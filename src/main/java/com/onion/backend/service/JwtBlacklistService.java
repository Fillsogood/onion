package com.onion.backend.service;

import com.onion.backend.JWT.JwtUtil;
import com.onion.backend.entity.JwtBlacklist;
import com.onion.backend.repository.JwtBlacklistRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtBlacklistService {


  private final JwtBlacklistRepository jwtBlacklistRepository;
  private final JwtUtil jwtUtil;

  @Autowired
  public JwtBlacklistService(JwtBlacklistRepository jwtBlacklistRepository, JwtUtil jwtUtil) {
    this.jwtBlacklistRepository = jwtBlacklistRepository;
    this.jwtUtil = jwtUtil;
  }

  // 블랙리스트에 토큰 추가
  @Transactional
  public void addToBlacklist(String token, LocalDateTime expiration, String username) {
    JwtBlacklist jwtBlacklist = new JwtBlacklist();
    jwtBlacklist.setToken(token);
    jwtBlacklist.setExpiresAt(expiration); // exp 클레임을 사용하여 토큰 만료 시간 설정
    jwtBlacklist.setUsername(username);
    jwtBlacklistRepository.save(jwtBlacklist);
  }

  // 블랙리스트에 토큰이 존재하는지 확인
  public boolean isTokenBlacklisted(String token) {
    Optional<JwtBlacklist> jwtBlacklist = jwtBlacklistRepository.findTop1ByTokenOrderByExpiresAtDesc(token);
    if (!jwtBlacklist.isPresent()) {
      return false;
    }
    // JWT 토큰의 만료 시간 얻기
    Instant instant = jwtUtil.getExpirationFromJwtToken(token).toInstant();
    LocalDateTime tokenExpirationTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

    // 블랙리스트에서 저장된 토큰의 만료 시간
    LocalDateTime blacklistExpirationTime = jwtBlacklist.get().getExpiresAt();

    // 블랙리스트에 저장된 토큰의 만료 시간이 현재 시간에서 1시간 이내이면 true 반환 (블랙리스트로 간주)
    return blacklistExpirationTime.isAfter(LocalDateTime.now().minusHours(1)) && tokenExpirationTime.isAfter(LocalDateTime.now());
  }
}

package com.onion.backend.repository;


import com.onion.backend.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {
  Optional<JwtBlacklist> findByToken(String token);
  Optional<JwtBlacklist> findTop1ByTokenOrderByExpiresAtDesc(String token);
}
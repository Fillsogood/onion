package com.onion.backend.repository;

import com.onion.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//  // 이메일로 사용자 찾기
//  User findByEmail(String email);
//
//  // 아이디로 사용자 찾기
  Optional<User> findByUsername(String username);


}


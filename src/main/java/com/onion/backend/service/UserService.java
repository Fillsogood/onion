package com.onion.backend.service;



import com.onion.backend.dto.SignUpUserDto;
import com.onion.backend.entity.User;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;
  @Autowired
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  // 유저 생성 메서드
  public User createUser(SignUpUserDto signUpUser ) {
    User newUser = new User();
    newUser.setUsername(signUpUser.getUsername());
    newUser.setPassword(passwordEncoder.encode(signUpUser.getPassword()));
    newUser.setEmail(signUpUser.getEmail());
    // 새로운 유저를 데이터베이스에 저장
    return userRepository.save(newUser);
  }

  // ID로 사용자 삭제
  public void deleteUserById(Long id) {
    userRepository.deleteById(id);
  }

  public List<User> getUsers() {
    return userRepository.findAll();
  }
}

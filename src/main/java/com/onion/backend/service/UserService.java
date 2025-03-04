package com.onion.backend.service;


import com.onion.backend.entity.User;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

//  private final BCryptPasswordEncoder passwordEncoder;
  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // 유저 생성 메서드
  public User createUser(String username, String password, String email) {
    User newUser = new User();
    newUser.setUsername(username);
    newUser.setPassword(password);
    newUser.setEmail(email);
    // 새로운 유저를 데이터베이스에 저장
    return userRepository.save(newUser);
  }

  // ID로 사용자 삭제
  public void deleteUserById(Long id) {
    userRepository.deleteById(id);
  }

}

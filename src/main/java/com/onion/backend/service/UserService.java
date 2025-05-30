package com.onion.backend.service;



import com.onion.backend.dto.SignUpUserDto;
import com.onion.backend.dto.WriteDeviceDto;
import com.onion.backend.entity.Device;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ResourcNotFoundException;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

  // 유저 전체 조회
  public List<User> getUsers() {
    return userRepository.findAll();
  }

  // 특정 id 유저 조회
  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }

  public List<Device> getDevices() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    // 현재 로그인한 사용자 찾기
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new ResourcNotFoundException("Author not found"));
    return user.getDevice();
  }

  public Device createDevice(WriteDeviceDto writeDeviceDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    // 현재 로그인한 사용자 찾기
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new ResourcNotFoundException("Author not found"));

    Device device = new Device();
    device.setDeviceName(writeDeviceDto.getDeviceName());
    device.setToken(writeDeviceDto.getToken());
    user.getDevice().add(device);
    userRepository.save(user);
    return device;
  }
}

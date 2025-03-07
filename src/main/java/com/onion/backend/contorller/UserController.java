package com.onion.backend.contorller;

import com.onion.backend.dto.SignUpUserDto;
import com.onion.backend.entity.User;
import com.onion.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.getUsers());
  }
  // 유저 생성 API
  @PostMapping("/signUp")
  public ResponseEntity<User> createUser(@Valid @RequestBody SignUpUserDto signUpUser) {
    User user = userService.createUser(signUpUser);
    return ResponseEntity.ok(user);
  }

  // ID로 사용자 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
    userService.deleteUserById(id);
    return ResponseEntity.ok("User deleted successfully");
  }

  // ID로 특정 유저 조회
  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    User user = userService.getUserById(id).get();
    return ResponseEntity.ok(user);
  }

}

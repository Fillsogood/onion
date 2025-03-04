package com.onion.backend.Contorller;

import com.onion.backend.entity.User;
import com.onion.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  // 유저 생성 API
  @PostMapping("")
  public ResponseEntity<User> createUser(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String email) {
    User user = userService.createUser(username, password, email);
    return ResponseEntity.ok(user);
  }

  // ID로 사용자 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
    userService.deleteUserById(id);
    return ResponseEntity.ok("User deleted successfully");
  }
}

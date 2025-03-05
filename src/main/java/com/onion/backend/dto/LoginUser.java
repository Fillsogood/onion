package com.onion.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginUser {

  @NotBlank(message = "아이디를 입력해주세요.") //컨트롤러에서 바로 Validation
  private String username;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  private String password;
}

package com.onion.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpUser {

  @NotBlank(message = "아이디를 입력해주세요.")  // 비어있지 않은 값 필수
  private String username;

  @NotBlank(message = "비밀번호를 입력해주세요.")  // 비어있지 않은 값 필수
  @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")  // 최소 6자리
  private String password;

  @NotBlank(message = "이메일을 입력해주세요.")  // 비어있지 않은 값 필수
  @Email(message ="잘못된 이메일 형식")  // 이메일 형식 체크
  private String email;
}
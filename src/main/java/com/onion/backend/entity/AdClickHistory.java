package com.onion.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "adClickHistory") // MongoDB 컬렉션 이름
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdClickHistory {

  @Id
  private String id;

  private Long adId;

  private String username;

  private String ip;

  private LocalDateTime createdAt = LocalDateTime.now();  // 생성일 (최초 가입일)

}
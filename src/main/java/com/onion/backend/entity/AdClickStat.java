package com.onion.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AdClickStat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long adId;

  private Long count;

  private String dt;

  @CreatedDate
  @Column(insertable = true)
  private LocalDateTime createdAt;  // 생성일 (최초 가입일)



  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}

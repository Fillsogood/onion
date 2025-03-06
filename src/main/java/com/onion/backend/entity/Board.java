package com.onion.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class Board {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;  // 로그인 ID

  @Column(nullable = false)
  private String description;  // 비밀번호
  @CreatedDate
  @Column(insertable = true)
  private LocalDateTime createdAt;  // 생성일 (최초 가입일)

  @LastModifiedDate
  private LocalDateTime updatedAt;  // 갱신일 (수정된 시간)

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = null; // 최초 생성 시에는 updatedAt이 null
  }

  // 수정 시 updatedAt을 갱신하는 방법
  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now(); // 수정 시에만 updatedAt을 갱신
  }
}

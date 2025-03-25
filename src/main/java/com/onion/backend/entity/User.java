package com.onion.backend.entity;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchConnectionDetails;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String username;  // 로그인 ID

  @JsonIgnore
  @Column(nullable = false)
  private String password;  // 비밀번호

  @Column(nullable = false, length = 100)
  private String email;     // 이메일

  @Convert(converter = DeviceListConverter.class)
  @Column(columnDefinition = "json")
  private List<Device> device = new ArrayList<>();

  private LocalDateTime lastLoginAt;  // 최근 로그인 시간

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

package com.onion.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "advertisment")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class Advertisment implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L; // 직렬화 ID 추가

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Lob
  @Column(nullable = false)
  private String content = "";

  @Column(nullable = false)
  private Boolean isDeleted = false;

  @Column(nullable = false)
  private Boolean isVisble = true;

  @Column(insertable = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startDate;

  @Column(insertable = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime endDate;

  @Column(nullable = false)
  private Integer clickCount = 0;

  @Column(nullable = false)
  private Integer viewCount = 0;


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

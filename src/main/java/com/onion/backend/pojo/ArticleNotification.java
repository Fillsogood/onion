package com.onion.backend.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ArticleNotification {
  private String type = "write_article";
  private Long user_id;
  private Long article_id;
}

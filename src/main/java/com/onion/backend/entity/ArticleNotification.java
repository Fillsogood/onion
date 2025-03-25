package com.onion.backend.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleNotification {
  private String type = "write_article";
  private Long user_id;
  private Long article_id;

  @Override
  public String toString() {
    return type + " " + user_id + " " + article_id;
  }
}

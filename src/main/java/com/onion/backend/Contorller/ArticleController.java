package com.onion.backend.Contorller;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.User;
import com.onion.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class ArticleController {

  private final AuthenticationManager authenticationManager;
  private final ArticleService articleService;

  @Autowired
  public ArticleController(ArticleService articleService, AuthenticationManager authenticationManager) {
    this.articleService = articleService;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/{boardId}/articles")
  public ResponseEntity<Article> writeArticle(@RequestBody WriteArticleDto writeArticleDto){
    // 현재 인증된 사용자 정보 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).build(); // 인증되지 않은 사용자 처리
    }

    return ResponseEntity.ok(articleService.writeArticle(writeArticleDto));
  }
}

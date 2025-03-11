package com.onion.backend.contorller;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.service.ArticleService;
import com.onion.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class ArticleController {

  private final ArticleService articleService;
  private final CommentService commentService;

  @Autowired
  public ArticleController(ArticleService articleService, CommentService commentService) {
    this.articleService = articleService;
    this.commentService = commentService;
  }

  @PostMapping("/{boardId}/articles")
  public ResponseEntity<Article> writeArticle(@RequestBody WriteArticleDto writeArticleDto, @PathVariable Long boardId) {
    // 현재 인증된 사용자 정보 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).build(); // 인증되지 않은 사용자 처리
    }

    return ResponseEntity.ok(articleService.writeArticle(writeArticleDto, boardId));
  }

  @GetMapping("/{boardId}/articles")
  public ResponseEntity<List<Article>> getArticles(@PathVariable Long boardId,
                                                   @RequestParam(required = false) Long lastId,
                                                   @RequestParam(required = false) Long firstId){
    if(lastId != null){
      return ResponseEntity.ok(articleService.getOldArticle(boardId, lastId));
    }
    if(firstId != null){
      return ResponseEntity.ok(articleService.getNewArticle(boardId, firstId));
    }
    return ResponseEntity.ok(articleService.firstGetArticle(boardId));
  }

  @PutMapping("/{boardId}/articles/{articleId}")
  public ResponseEntity<Article> editArticles(@PathVariable Long boardId,@PathVariable Long articleId,
                                                    @RequestBody WriteArticleDto writeArticleDto){
    return ResponseEntity.ok(articleService.editArticle(boardId,writeArticleDto, articleId));
  }

  @DeleteMapping("/{boardId}/articles/{articleId}")
  public ResponseEntity<String> deleteArticle(@PathVariable Long boardId,@PathVariable Long articleId){
    articleService.deleteArticle(boardId,articleId);
    return ResponseEntity.ok("Delete article");
  }

  @GetMapping("/{boardId}/articles/{articleId}")
  public ResponseEntity<Article> getArticleWithComment(@PathVariable Long boardId, @PathVariable Long articleId){
    Article article = commentService.getArticleWithComment(articleId,boardId).join();
    return ResponseEntity.ok(article);
  }
}

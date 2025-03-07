package com.onion.backend.contorller;

import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Comment;
import com.onion.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/boards")
public class CommentController {

  private final CommentService commentService;

  @Autowired
  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("/{boardId}/articles/{articleId}")
  public ResponseEntity<Comment> writeComment(@RequestBody WriteCommentDto writeCommentDto, @PathVariable Long articleId) {
    return ResponseEntity.ok(commentService.writeComment(writeCommentDto,articleId));
  }

//  @GetMapping("/comment/{commentId}")
//  public ResponseEntity<List<Article>> getArticles(@PathVariable Long boardId,
//                                                   @RequestParam(required = false) Long lastId,
//                                                   @RequestParam(required = false) Long firstId){
//    if(lastId != null){
//      return ResponseEntity.ok(articleService.getOldArticle(boardId, lastId));
//    }
//    if(firstId != null){
//      return ResponseEntity.ok(articleService.getNewArticle(boardId, firstId));
//    }
//    return ResponseEntity.ok(articleService.firstGetArticle(boardId));
//  }

//  @PutMapping("/{boardId}/articles/{articleId}")
//  public ResponseEntity<Article> editArticles(@PathVariable Long boardId,@PathVariable Long articleId,
//                                              @RequestBody WriteArticleDto writeArticleDto){
//    return ResponseEntity.ok(articleService.editArticle(boardId,writeArticleDto, articleId));
//  }
//
//  @DeleteMapping("/{boardId}/articles/{articleId}")
//  public ResponseEntity<String> deleteArticle(@PathVariable Long boardId,@PathVariable Long articleId){
//    articleService.deleteArticle(boardId,articleId);
//    return ResponseEntity.ok("Delete article");
//  }
}

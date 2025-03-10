package com.onion.backend.contorller;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Article;
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


  @PutMapping("/{boardId}/articles/{articleId}/{commentId}")
  public ResponseEntity<Comment> editComment(@PathVariable Long boardId, @PathVariable Long articleId,
                                              @RequestBody WriteCommentDto writeCommentDto,@PathVariable Long commentId){
    return ResponseEntity.ok(commentService.editComment(boardId,writeCommentDto, articleId, commentId));
  }

  @DeleteMapping("/{boardId}/articles/{articleId}/{commentId}")
  public ResponseEntity<String> deleteArticle(@PathVariable Long boardId,@PathVariable Long articleId,@PathVariable Long commentId){
    commentService.deleteComment(boardId,articleId,commentId);
    return ResponseEntity.ok("Delete comment");
  }
}

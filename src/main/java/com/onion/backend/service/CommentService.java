package com.onion.backend.service;

import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Comment;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourcNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import com.onion.backend.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class CommentService {
  private final ArticleRepository articleRepository;
  private final BoardRepository boardRepository;
  private final UserRepository userRepository;

  private final CommentRepository commentRepository;

  @Autowired
  public CommentService(ArticleRepository articleRepository, BoardRepository boardRepository, UserRepository userRepository, CommentRepository commentRepository) {
    this.articleRepository = articleRepository;
    this.boardRepository = boardRepository;
    this.userRepository = userRepository;
    this.commentRepository = commentRepository;
  }
  @Transactional
  public Comment writeComment(WriteCommentDto writeCommentDto, Long articleId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    // 수정 시간 체크
    long remainingTime = getRemainingWriteCooldown(userDetails.getUsername());
    if (remainingTime > 0) {
      throw new RateLimitException("You can edit this article in " + remainingTime + " minutes.");
    }

    // 현재 로그인한 사용자 찾기
    User author = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new ResourcNotFoundException("Author not found"));

    // 게시글 존재 여부 확인
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourcNotFoundException("Article with id " + articleId + " not found"));

    // 게시글 삭제 여부 확인
    if(article.getIsDeleted()) {
      throw new ForbiddenException("Article with id " + articleId + " is deleted");
    }

    // 새로운 댓글 생성 및 저장
    Comment comment = new Comment();
    comment.setArticle(article);
    comment.setAuthor(author);
    comment.setContent(writeCommentDto.getContent());
    commentRepository.save(comment);
    return comment;
  }

  @Async
  protected CompletableFuture<Article> getArticle(Long articleId, Long boardId) {
    // 게시판 존재 여부 확인
    boardRepository.findById(boardId)
        .orElseThrow(() -> new ResourcNotFoundException("Board with id " + boardId + " not found"));

    // 게시글 존재 여부 확인
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourcNotFoundException("Article with id " + articleId + " not found"));

    // 게시글 삭제 여부 확인
    if(article.getIsDeleted()) {
      throw new ForbiddenException("Article with id " + articleId + " is deleted");
    }

    return CompletableFuture.completedFuture(article);
  }

  @Async
  protected CompletableFuture<List<Comment>> getComments(Long articleId) {
    return CompletableFuture.completedFuture(commentRepository.findAllByArticle(articleId));
  }

  public CompletableFuture<Article> getArticleWithComment(Long articleId, Long boardId) {
    CompletableFuture<Article> getArticleResult = this.getArticle(articleId, boardId);
    CompletableFuture<List<Comment>> getCommentsResult = this.getComments(articleId);

    // thenCombine()을 사용하여 두 개의 결과를 조합하여 반환
    return getArticleResult.thenCombine(getCommentsResult, (article, comments) -> {
      article.setComments(comments);
      return article;
    });
  }



  // 사용자가 마지막으로 작성한 글이 1분 이상 지났는지 검증
  public long getRemainingWriteCooldown(String username) {
    return commentRepository.findTopByAuthorUsernameOrderByCreatedAtDesc(username)
        .map(lastWrittenArticle -> {
          if (lastWrittenArticle.getCreatedAt() != null) {
            return this.getRemainingCooldownTime(lastWrittenArticle.getCreatedAt());
          }
          return 0L; // 바로 작성 가능
        })
        .orElse(0L); // 최근 작성된 글이 없으면 바로 작성 가능
  }

  //사용자가 마지막으로 수정한 글인지 확인하고, 1분 이상 지났는지 검증
//  public long getRemainingEditCooldown(String username, Article article) {
//    return commentRepository.findTopByAuthorUsernameOrderByUpdatedAtDesc(username)
//        .map(lastEditedArticle -> {
//          if (lastEditedArticle.getId().equals(article.getId()) && lastEditedArticle.getUpdatedAt() != null) {
//            return this.getRemainingCooldownTime(lastEditedArticle.getUpdatedAt());
//          }
//          return 0L; // 바로 수정 가능
//        })
//        .orElse(0L); // 최근 수정된 글이 없으면 바로 수정 가능
//  }

  //특정 시간(localDateTime)이 현재 시간보다 5분 이상 지났는지 확인
  public long getRemainingCooldownTime(LocalDateTime lastUpdatedTime) {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.between(lastUpdatedTime, now);
    long elapsedMinutes = duration.toMinutes();
    return Math.max(1 - elapsedMinutes, 0); // 1분 이상 지났으면 0 반환
  }
}

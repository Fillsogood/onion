package com.onion.backend.service;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourcNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final BoardRepository boardRepository;
  private final UserRepository userRepository;
  private final ElasticSearchService elasticSearchService;

  @Autowired
  public ArticleService(ArticleRepository articleRepository, BoardRepository boardRepository, UserRepository userRepository, ElasticSearchService elasticSearchService) {
    this.articleRepository = articleRepository;
    this.boardRepository = boardRepository;
    this.userRepository = userRepository;
    this.elasticSearchService = elasticSearchService;
  }

  @Transactional
  public Article writeArticle(WriteArticleDto writeArticleDto, Long boardId) {
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

    // 게시판 존재 여부 확인
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new ResourcNotFoundException("Board with id " + boardId + " not found"));


    // 새로운 게시글 생성 및 저장
    Article article = new Article();
    article.setBoard(board);
    article.setAuthor(author);
    article.setTitle(writeArticleDto.getTitle());
    article.setContent(writeArticleDto.getContent());
    articleRepository.save(article);

    // 🔹 Elasticsearch 색인 (비동기 실행)
    elasticSearchService.indexArticle(article)
        .subscribe(
            result -> System.out.println("Elasticsearch index success: " + result),
            error -> System.err.println("Elasticsearch index failed: " + error.getMessage()) //예외 처리 추가
        );
    return article;
  }

  // 특정 최신 게시글 목록 (최대 10개) 조회
  public List<Article> firstGetArticle(Long boardId) {
    return articleRepository.findTop10ByBoardIdAndIsDeletedFalseOrderByCreatedAtDesc(boardId);
  }

  // 특정 게시판에서 주어진 게시글 ID보다 오래된 게시글 10개 조회
  public List<Article> getOldArticle(Long boardId, long articleId) {
    return articleRepository.findTop10ByBoardIdAndIdLessThanAndIsDeletedFalseOrderByCreatedAtDesc(boardId, articleId);
  }

  // 특정 게시판에서 주어진 게시글 ID보다 새로운 게시글 10개 조회
  public List<Article> getNewArticle(Long boardId, long articleId) {
    return articleRepository.findTop10ByBoardIdAndIdGreaterThanAndIsDeletedFalseOrderByCreatedAtAsc(boardId, articleId);
  }


  @Transactional
  public Article editArticle(Long boardId, WriteArticleDto writeArticleDto, Long articleId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String author = userDetails.getUsername();

    // 현재 로그인한 사용자 찾기
    userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new ResourcNotFoundException("Author not found"));

    // 게시판 존재 여부 확인
    boardRepository.findById(boardId)
        .orElseThrow(() -> new ResourcNotFoundException("Board with id " + boardId + " not found"));

    // 게시글 존재 여부 확인
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourcNotFoundException("Article with id " + articleId + " not found"));

    // 현재 사용자가 작성자와 일치하는지 확인
    if (!article.getAuthor().getUsername().equals(author)) {
      throw new ForbiddenException("Author different from article author");
    }

    // 수정 시간 체크
    long remainingTime = getRemainingEditCooldown(author, article);
    if (remainingTime > 0) {
      throw new RateLimitException("You can edit this article in " + remainingTime + " minutes.");
    }

    if(!writeArticleDto.getTitle().isEmpty()){
      article.setTitle(writeArticleDto.getTitle());
    }
    if(!writeArticleDto.getContent().isEmpty()){
      article.setContent(writeArticleDto.getContent());
    }
    articleRepository.save(article);
    elasticSearchService.indexArticle(article)
        .subscribe(
            result -> System.out.println("Elasticsearch index success: " + result),
            error -> System.err.println("Elasticsearch index failed: " + error.getMessage()) // 예외 처리 추가
        );
    return article;
  }

  @Transactional
  public boolean deleteArticle(Long boardId, Long articleId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String author = userDetails.getUsername();

    // 현재 로그인한 사용자 찾기
    userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new ResourcNotFoundException("Author not found"));

    // 게시판 존재 여부 확인
    boardRepository.findById(boardId)
        .orElseThrow(() -> new ResourcNotFoundException("Board with id " + boardId + " not found"));

    // 게시글 존재 여부 확인
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourcNotFoundException("Article with id " + articleId + " not found"));

    // 현재 사용자가 작성자와 일치하는지 확인
    if (!article.getAuthor().getUsername().equals(author)) {
      throw new ForbiddenException("Author different from article author");
    }

    // 수정 시간 체크
    long remainingTime = getRemainingEditCooldown(author, article);
    if (remainingTime > 0) {
      throw new RateLimitException("You can edit this article in " + remainingTime + " minutes.");
    }

    article.setIsDeleted(true);
    articleRepository.save(article);
    elasticSearchService.indexArticle(article)
        .subscribe(
            result -> System.out.println("Elasticsearch index success: " + result),
            error -> System.err.println("Elasticsearch index failed: " + error.getMessage()) // 예외 처리 추가
        );
    return true;
  }

  // 사용자가 마지막으로 작성한 글이 5분 이상 지났는지 검증
  public long getRemainingWriteCooldown(String username) {
    return articleRepository.findTopByAuthorUsernameOrderByCreatedAtDesc(username)
        .map(lastWrittenArticle -> {
          if (lastWrittenArticle.getCreatedAt() != null) {
            return this.getRemainingCooldownTime(lastWrittenArticle.getCreatedAt());
          }
          return 0L; // 바로 작성 가능
        })
        .orElse(0L); // 최근 작성된 글이 없으면 바로 작성 가능
  }

  //사용자가 마지막으로 수정한 글인지 확인하고, 5분 이상 지났는지 검증
  public long getRemainingEditCooldown(String username, Article article) {
    return articleRepository.findTopByAuthorUsernameOrderByUpdatedAtDesc(username)
        .map(lastEditedArticle -> {
          if (lastEditedArticle.getId().equals(article.getId()) && lastEditedArticle.getUpdatedAt() != null) {
            return this.getRemainingCooldownTime(lastEditedArticle.getUpdatedAt());
          }
          return 0L; // 바로 수정 가능
        })
        .orElse(0L); // 최근 수정된 글이 없으면 바로 수정 가능
  }

  //특정 시간(localDateTime)이 현재 시간보다 5분 이상 지났는지 확인
  public long getRemainingCooldownTime(LocalDateTime lastUpdatedTime) {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.between(lastUpdatedTime, now);
    long elapsedMinutes = duration.toMinutes();
    return Math.max(5 - elapsedMinutes, 0); // 5분 이상 지났으면 0 반환
  }

  public List<Article> searchArticle(String keyword) {
      Mono<List<Long>> articleIds = elasticSearchService.articleSearch(keyword);
    try {
      return articleRepository.findAllById(articleIds.toFuture().get());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}

package com.onion.backend.service;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourcNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final BoardRepository boardRepository;
  private final UserRepository userRepository;

  @Autowired
  public ArticleService(ArticleRepository articleRepository, BoardRepository boardRepository, UserRepository userRepository) {
    this.articleRepository = articleRepository;
    this.boardRepository = boardRepository;
    this.userRepository = userRepository;
  }

  public Article writeArticle(WriteArticleDto writeArticleDto, Long boardId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    // 사용자가 5분 내에 글을 작성했는지 검사
    if(!this.isLastWriteArticle()){
      throw new RateLimitException("article not write by rate limit");
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
    return article;
  }

  // 특정 최신 게시글 목록 (최대 10개) 조회
  public List<Article> firstGetArticle(Long boardId) {
    return articleRepository.findTop10ByBoardIdOrderByCreatedAtDesc(boardId);
  }

  // 특정 게시판에서 주어진 게시글 ID보다 오래된 게시글 10개 조회
  public List<Article> getOldArticle(Long boardId, long articleId) {
    return articleRepository.findTop10ByBoardIdAndIdLessThanOrderByCreatedAtDesc(boardId, articleId);
  }

  // 특정 게시판에서 주어진 게시글 ID보다 새로운 게시글 10개 조회
  public List<Article> getNewArticle(Long boardId, long articleId) {
    return articleRepository.findTop10ByBoardIdAndIdGreaterThanOrderByCreatedAtAsc(boardId, articleId);
  }

  public Article editArticle(Long boardId, WriteArticleDto writeArticleDto, Long articleId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String username = userDetails.getUsername();
    // 현재 로그인한 사용자 찾기
    userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new ResourcNotFoundException("Author not found"));

    // 게시판 존재 여부 확인
    boardRepository.findById(boardId)
        .orElseThrow(() -> new ResourcNotFoundException("Board with id " + boardId + " not found"));

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourcNotFoundException("Article with id " + articleId + " not found"));

    // 현재 사용자가 마지막으로 수정한 글인지 확인 & 5분 제한 체크
    if (!isLastEditArticle(username, article)) {
      throw new RateLimitException("Article edit rate limit exceeded.");
    }
    if(!writeArticleDto.getTitle().isEmpty()){
      article.setTitle(writeArticleDto.getTitle());
    }
    if(!writeArticleDto.getContent().isEmpty()){
      article.setContent(writeArticleDto.getContent());
    }
    articleRepository.save(article);
    return article;
  }

  // // 사용자가 마지막으로 작성한 글이 5분 이상 지났는지 검증
  private boolean isLastWriteArticle() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    return articleRepository.findTopByAuthorUsernameOrderByCreatedAtDesc(userDetails.getUsername())
        .map(article -> this.isDifferenceMoreThanFiveMinutes(article.getCreatedAt()))
        .orElse(true);
  }

  //사용자가 마지막으로 수정한 글인지 확인하고, 5분 이상 지났는지 검증
  private boolean isLastEditArticle(String username, Article article) {
    return articleRepository.findTopByAuthorUsernameOrderByUpdatedAtDesc(username)
        .map(lastEditedArticle -> lastEditedArticle.getId().equals(article.getId()) &&
            lastEditedArticle.getUpdatedAt() != null &&
            this.isDifferenceMoreThanFiveMinutes(lastEditedArticle.getUpdatedAt()))
        .orElse(false);
  }

  //특정 시간(localDateTime)이 현재 시간보다 5분 이상 지났는지 확인
  public boolean isDifferenceMoreThanFiveMinutes(LocalDateTime localDateTime) {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.between(localDateTime, now);

    return duration.toMinutes() >= 5; // 5분 이상 지나야 true (수정 가능)
  }
}

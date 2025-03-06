package com.onion.backend.service;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ResourcNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

  public Article writeArticle(WriteArticleDto writeArticleDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
    Optional<Board> board = boardRepository.findById(writeArticleDto.getBoardId());
    if (author.isEmpty()){
      throw new ResourcNotFoundException("author not found");
    }
    if(board.isEmpty()) {
        throw new ResourcNotFoundException("Board with id " + writeArticleDto.getBoardId() + " not found");
    }
    Article article = new Article();
    article.setBoard(board.get());
    article.setAuthor(author.get());
    article.setTitle(writeArticleDto.getTitle());
    article.setContent(writeArticleDto.getContent());
    articleRepository.save(article);
    return article;
  }
}

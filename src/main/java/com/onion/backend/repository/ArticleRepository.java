package com.onion.backend.repository;

import com.onion.backend.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
  List<Article> findTop10ByBoardIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("boardId") Long boardId);

  List<Article> findTop10ByBoardIdAndIdLessThanAndIsDeletedFalseOrderByCreatedAtDesc(
      @Param("boardId") Long boardId, @Param("id") Long id);

  List<Article> findTop10ByBoardIdAndIdGreaterThanAndIsDeletedFalseOrderByCreatedAtAsc(
      @Param("boardId") Long boardId, @Param("id") Long id);
  Optional<Article> findTopByAuthorUsernameOrderByCreatedAtDesc(@Param("username") String username);

  Optional<Article> findTopByAuthorUsernameOrderByUpdatedAtDesc(@Param("username") String username);


}


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

  // isDeleted가 false인 Article들을 boardId 기준으로 최신순(내림차순)으로 10개 조회
  @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.isDeleted = false ORDER BY a.createdAt DESC LIMIT 10")
  List<Article> findTop10ByBoardIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("boardId") Long boardId);

  // isDeleted가 false인 Article들 중, boardId가 일치하고, id가 특정 값보다 작은 글을 최신순(내림차순)으로 10개 조회
  @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id < :id AND a.isDeleted = false ORDER BY a.createdAt DESC LIMIT 10")
  List<Article> findTop10ByBoardIdAndIdLessThanAndIsDeletedFalseOrderByCreatedAtDesc(@Param("boardId") Long boardId,
                                                                                     @Param("id") Long id);

  // isDeleted가 false인 Article들 중, boardId가 일치하고, id가 특정 값보다 큰 글을 오름차순(최신순)으로 10개 조회
  @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id > :id AND a.isDeleted = false ORDER BY a.createdAt ASC LIMIT 10")
  List<Article> findTop10ByBoardIdAndIdGreaterThanAndIsDeletedFalseOrderByCreatedAtAsc(@Param("boardId") Long boardId,
                                                                                       @Param("id") Long id);

  // 작성자의 최신 Article 조회 (작성일 기준 내림차순)
  @Query("SELECT a FROM Article a JOIN a.author u WHERE  u.username = :username ORDER BY a.createdAt DESC LIMIT 1")
  Optional<Article> findTopByAuthorUsernameOrderByCreatedAtDesc(@Param("username") String username);

  // 작성자의 최신 수정 Article 조회 (수정일 기준 내림차순)
  @Query("SELECT a FROM Article a JOIN a.author u WHERE  u.username = :username ORDER BY a.updatedAt DESC LIMIT 1")
  Optional<Article> findTopByAuthorUsernameOrderByUpdatedAtDesc(@Param("username") String username);
}

package com.onion.backend.repository;

import com.onion.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  // 작성자의 최신 댓글 조회 (작성일 기준 내림차순)
  @Query("SELECT c FROM Comment c JOIN c.author u WHERE u.username = :username AND c.isDeleted = false ORDER BY c.createdAt DESC limit 1")
  Optional<Comment> findTopByAuthorUsernameOrderByCreatedAtDesc(@Param("username") String username);

  // 작성자의 최신 수정 댓글 조회 (수정일 기준 내림차순)
  @Query("SELECT c FROM Comment c JOIN c.author u WHERE u.username = :username AND c.isDeleted = false ORDER BY c.updatedAt DESC limit 1")
  Optional<Comment> findTopByAuthorUsernameOrderByUpdatedAtDesc(@Param("username") String username);

  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false ")
  List<Comment> findAllByArticle(@Param("articleId") Long articleId);

  @Query("SELECT c FROM Comment c WHERE c.author.username = :username AND c.isDeleted = true ORDER BY c.updatedAt DESC limit 1")
  Optional<Comment> findTopByAuthorUsernameAndIsDeletedTrueOrderByUpdatedAtDesc(@Param("username") String username);

  @Query("SELECT c.article.id FROM Comment c WHERE c.id = :id")
  Long findArticleIdByCommentId(@Param("id") Long commentId);

  @Query("SELECT c.article.author.id FROM Comment c WHERE c.id = :id")
  Long findArticleAuthorIdByCommentId(@Param("id") Long commentId);

  @Query("SELECT DISTINCT c.author.id FROM Comment c WHERE c.article.id = :articleId")
  List<Long> findAllCommentUserIdsByArticleId(@Param("articleId") Long articleId);

  @Query("SELECT c.author.id FROM Comment c WHERE c.id = :id")
  Long findCommentAuthorIdByCommentId(@Param("id") Long commentId);

}
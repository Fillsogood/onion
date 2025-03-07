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

  // 특정 게시글(articleId)에 작성된 댓글 중, 삭제되지 않은 댓글을 최신순(내림차순)으로 10개 조회
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false ORDER BY c.createdAt DESC")
  List<Comment> findTop10ByArticleIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("articleId") Long articleId);
}

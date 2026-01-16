package com.kindcircle.repository;

import com.kindcircle.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(Long postId);
}

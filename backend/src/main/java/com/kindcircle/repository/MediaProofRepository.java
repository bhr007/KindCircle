package com.kindcircle.repository;

import com.kindcircle.model.MediaProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaProofRepository extends JpaRepository<MediaProof, Long> {
    @Query("SELECT m FROM MediaProof m WHERE m.post.id = :postId ORDER BY m.createdAt ASC")
    List<MediaProof> findByPostId(@Param("postId") Long postId);
}

package com.kindcircle.repository;

import com.kindcircle.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndPostId(Long userId, Long postId);
    long countByPostIdAndVoteType(Long postId, Vote.VoteType voteType);
    
    // Count upvotes on all posts by a specific user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.user.id = :userId AND v.voteType = 'UPVOTE'")
    long countUpvotesByPostOwnerId(@Param("userId") Long userId);
    
    // Count downvotes on all posts by a specific user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.user.id = :userId AND v.voteType = 'DOWNVOTE'")
    long countDownvotesByPostOwnerId(@Param("userId") Long userId);
}

package com.kindcircle.repository;

import com.kindcircle.model.SocialWorkPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialWorkPostRepository extends JpaRepository<SocialWorkPost, Long> {
    List<SocialWorkPost> findByVerificationStatusOrderByCreatedAtDesc(SocialWorkPost.VerificationStatus status);
    
    @Query("SELECT p FROM SocialWorkPost p WHERE p.verificationStatus = 'APPROVED' ORDER BY p.createdAt DESC")
    List<SocialWorkPost> findApprovedPostsOrderByRecent();
    
    @Query("SELECT p FROM SocialWorkPost p WHERE p.verificationStatus = 'APPROVED' ORDER BY p.impactScoreEarned DESC, p.createdAt DESC")
    List<SocialWorkPost> findApprovedPostsOrderByImpactful();
    
    List<SocialWorkPost> findByVerificationStatus(SocialWorkPost.VerificationStatus status);
    
    List<SocialWorkPost> findByUserId(Long userId);
}

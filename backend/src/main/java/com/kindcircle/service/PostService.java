package com.kindcircle.service;

import com.kindcircle.dto.PostRequest;
import com.kindcircle.dto.PostResponse;
import com.kindcircle.model.*;
import com.kindcircle.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class PostService {
    
    @Autowired
    private SocialWorkPostRepository postRepository;
    
    @Autowired
    private MediaProofRepository mediaProofRepository;
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ImpactScoreService impactScoreService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private CommentVoteRepository commentVoteRepository;
    
    public List<PostResponse> getApprovedPosts(String sortBy) {
        List<SocialWorkPost> allPosts = postRepository.findApprovedPostsOrderByRecent();
        
        // Load all posts with their vote and comment counts
        List<PostResponse> postResponses = allPosts.stream()
            .map(post -> {
                // Explicitly load media proofs using repository
                List<com.kindcircle.model.MediaProof> mediaProofs = mediaProofRepository.findByPostId(post.getId());
                if (mediaProofs == null) {
                    mediaProofs = new ArrayList<>();
                }
                post.setMediaProofs(mediaProofs);
                
                Long upvotes = voteRepository.countByPostIdAndVoteType(post.getId(), Vote.VoteType.UPVOTE);
                Long downvotes = voteRepository.countByPostIdAndVoteType(post.getId(), Vote.VoteType.DOWNVOTE);
                Long commentsCount = (long) commentRepository.findByPostIdOrderByCreatedAtDesc(post.getId()).size();
                return PostResponse.fromPost(post, upvotes, downvotes, commentsCount);
            })
            .collect(Collectors.toList());
        
        // Apply sorting
        switch (sortBy.toLowerCase()) {
            case "impactful":
                // Sort by impact score, then by votes
                postResponses.sort((a, b) -> {
                    int impactDiff = Integer.compare(b.getImpactScoreEarned(), a.getImpactScoreEarned());
                    if (impactDiff != 0) return impactDiff;
                    long scoreA = a.getUpvotes() - a.getDownvotes();
                    long scoreB = b.getUpvotes() - b.getDownvotes();
                    return Long.compare(scoreB, scoreA);
                });
                break;
            case "trending":
            case "popular":
                // Sort by engagement (upvotes - downvotes + comments * 2), then by date
                postResponses.sort((a, b) -> {
                    long engagementA = (a.getUpvotes() - a.getDownvotes()) + (a.getCommentsCount() * 2);
                    long engagementB = (b.getUpvotes() - b.getDownvotes()) + (b.getCommentsCount() * 2);
                    if (engagementA != engagementB) {
                        return Long.compare(engagementB, engagementA);
                    }
                    // If engagement is same, sort by date (newer first)
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                });
                break;
            case "votes":
            case "top":
                // Sort by net votes (upvotes - downvotes), then by total votes
                postResponses.sort((a, b) -> {
                    long scoreA = a.getUpvotes() - a.getDownvotes();
                    long scoreB = b.getUpvotes() - b.getDownvotes();
                    if (scoreA != scoreB) {
                        return Long.compare(scoreB, scoreA);
                    }
                    // If net votes are same, sort by total votes
                    long totalA = a.getUpvotes() + a.getDownvotes();
                    long totalB = b.getUpvotes() + b.getDownvotes();
                    return Long.compare(totalB, totalA);
                });
                break;
            default: // "recent" or any other
                // Sort by date (newest first) - already sorted from repository
                break;
        }
        
        return postResponses;
    }
    
    @Transactional
    public PostResponse createPost(PostRequest request, List<MultipartFile> mediaFiles) {
        User currentUser = getCurrentUser();
        
        SocialWorkPost post = new SocialWorkPost();
        post.setUser(currentUser);
        post.setActivityType(SocialWorkPost.ActivityType.valueOf(request.getActivityType()));
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setLocation(request.getLocation());
        post.setActivityDate(request.getActivityDate());
        
        // Set post as PENDING - requires admin approval
        post.setVerificationStatus(SocialWorkPost.VerificationStatus.PENDING);
        // Impact score will be assigned when admin approves
        post.setImpactScoreEarned(0);
        
        SocialWorkPost savedPost = postRepository.save(post);
        
        // Save media files to disk and store paths in database
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (MultipartFile file : mediaFiles) {
                if (!file.isEmpty()) {
                    try {
                        // Save file to disk
                        String filePath = fileStorageService.savePostMedia(file);
                        
                        // Determine media type
                        MediaProof.MediaType mediaType = file.getContentType().startsWith("video/") 
                                ? MediaProof.MediaType.VIDEO 
                                : MediaProof.MediaType.PHOTO;
                        
                        // Save file path in database
                        MediaProof proof = new MediaProof();
                        proof.setPost(savedPost);
                        proof.setMediaType(mediaType);
                        proof.setMediaUrl(filePath); // Store file path, not base64
                        proof.setMimeType(file.getContentType());
                        proof.setFileSize(file.getSize());
                        mediaProofRepository.save(proof);
                    } catch (Exception e) {
                        // Continue with other files even if one fails
                    }
                }
            }
        }
        
        // Impact score and user stats will be updated when admin approves the post
        
        // Load media proofs explicitly
        List<com.kindcircle.model.MediaProof> mediaProofs = mediaProofRepository.findByPostId(savedPost.getId());
        if (mediaProofs == null) {
            mediaProofs = new ArrayList<>();
        }
        savedPost.setMediaProofs(mediaProofs);
        
        Long upvotes = voteRepository.countByPostIdAndVoteType(savedPost.getId(), Vote.VoteType.UPVOTE);
        Long downvotes = voteRepository.countByPostIdAndVoteType(savedPost.getId(), Vote.VoteType.DOWNVOTE);
        Long commentsCount = (long) commentRepository.findByPostIdOrderByCreatedAtDesc(savedPost.getId()).size();
        
        return PostResponse.fromPost(savedPost, upvotes, downvotes, commentsCount);
    }
    
    @Transactional
    public void votePost(Long postId, String voteType) {
        User currentUser = getCurrentUser();
        SocialWorkPost post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        Vote.VoteType type = Vote.VoteType.valueOf(voteType);
        final boolean[] isNewVote = {false};
        final boolean[] voteTypeChanged = {false};
        final Vote.VoteType[] previousVoteType = {null};
        
        // Check if user already voted
        voteRepository.findByUserIdAndPostId(currentUser.getId(), postId)
            .ifPresentOrElse(
                existingVote -> {
                    previousVoteType[0] = existingVote.getVoteType();
                    if (existingVote.getVoteType() == type) {
                        // Remove vote if same type clicked again
                        voteRepository.delete(existingVote);
                    } else {
                        // Update vote type - user changed their vote
                        existingVote.setVoteType(type);
                        voteRepository.save(existingVote);
                        voteTypeChanged[0] = true;
                    }
                },
                () -> {
                    // Create new vote
                    Vote vote = new Vote();
                    vote.setPost(post);
                    vote.setUser(currentUser);
                    vote.setVoteType(type);
                    voteRepository.save(vote);
                    isNewVote[0] = true;
                }
            );
        
        // Update post owner's upvote/downvote counts
        if (!post.getUser().getId().equals(currentUser.getId())) {
            User postOwner = post.getUser();
            
            if (isNewVote[0]) {
                // New vote added
                if (type == Vote.VoteType.UPVOTE) {
                    postOwner.setUpvotesReceived(postOwner.getUpvotesReceived() + 1);
                } else {
                    postOwner.setDownvotesReceived(postOwner.getDownvotesReceived() + 1);
                }
            } else if (voteTypeChanged[0] && previousVoteType[0] != null) {
                // Vote type changed - adjust counts
                if (previousVoteType[0] == Vote.VoteType.UPVOTE) {
                    postOwner.setUpvotesReceived(Math.max(0, postOwner.getUpvotesReceived() - 1));
                } else {
                    postOwner.setDownvotesReceived(Math.max(0, postOwner.getDownvotesReceived() - 1));
                }
                
                if (type == Vote.VoteType.UPVOTE) {
                    postOwner.setUpvotesReceived(postOwner.getUpvotesReceived() + 1);
                } else {
                    postOwner.setDownvotesReceived(postOwner.getDownvotesReceived() + 1);
                }
            } else if (!isNewVote[0] && !voteTypeChanged[0]) {
                // Vote was removed
                if (previousVoteType[0] == Vote.VoteType.UPVOTE) {
                    postOwner.setUpvotesReceived(Math.max(0, postOwner.getUpvotesReceived() - 1));
                } else if (previousVoteType[0] == Vote.VoteType.DOWNVOTE) {
                    postOwner.setDownvotesReceived(Math.max(0, postOwner.getDownvotesReceived() - 1));
                }
            }
            
            userService.save(postOwner);
        }
        
        // Create notification for new votes or when vote type changes (if user is voting on someone else's post)
        if (!post.getUser().getId().equals(currentUser.getId()) && (isNewVote[0] || voteTypeChanged[0])) {
            String message = type == Vote.VoteType.UPVOTE 
                ? currentUser.getFullName() + " upvoted your post"
                : currentUser.getFullName() + " downvoted your post";
            Notification.NotificationType notificationType = type == Vote.VoteType.UPVOTE 
                ? Notification.NotificationType.UPVOTE 
                : Notification.NotificationType.DOWNVOTE;
            notificationService.createNotification(
                post.getUser().getId(),
                notificationType,
                message,
                postId,
                currentUser.getId()
            );
        }
    }
    
    public List<PostResponse> getPendingPosts() {
        List<SocialWorkPost> posts = postRepository.findByVerificationStatus(
            SocialWorkPost.VerificationStatus.PENDING
        );
        
        return posts.stream()
            .map(post -> {
                // Explicitly load media proofs
                List<com.kindcircle.model.MediaProof> mediaProofs = mediaProofRepository.findByPostId(post.getId());
                if (mediaProofs == null) {
                    mediaProofs = new ArrayList<>();
                }
                post.setMediaProofs(mediaProofs);
                
                Long upvotes = voteRepository.countByPostIdAndVoteType(post.getId(), Vote.VoteType.UPVOTE);
                Long downvotes = voteRepository.countByPostIdAndVoteType(post.getId(), Vote.VoteType.DOWNVOTE);
                Long commentsCount = (long) commentRepository.findByPostIdOrderByCreatedAtDesc(post.getId()).size();
                return PostResponse.fromPost(post, upvotes, downvotes, commentsCount);
            })
            .collect(Collectors.toList());
    }
    
    public List<PostResponse> getPostsByUserId(Long userId) {
        List<SocialWorkPost> posts = postRepository.findByUserId(userId);
        
        return posts.stream()
            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt())) // Sort by most recent first
            .map(post -> {
                // Explicitly load media proofs
                List<com.kindcircle.model.MediaProof> mediaProofs = mediaProofRepository.findByPostId(post.getId());
                if (mediaProofs == null) {
                    mediaProofs = new ArrayList<>();
                }
                post.setMediaProofs(mediaProofs);
                
                Long upvotes = voteRepository.countByPostIdAndVoteType(post.getId(), Vote.VoteType.UPVOTE);
                Long downvotes = voteRepository.countByPostIdAndVoteType(post.getId(), Vote.VoteType.DOWNVOTE);
                Long commentsCount = (long) commentRepository.findByPostIdOrderByCreatedAtDesc(post.getId()).size();
                return PostResponse.fromPost(post, upvotes, downvotes, commentsCount);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public PostResponse verifyPost(Long postId, boolean approved, String rejectionReason) {
        User admin = getCurrentUser();
        
        SocialWorkPost post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Check if post is already verified
        if (post.getVerificationStatus() != SocialWorkPost.VerificationStatus.PENDING) {
            throw new RuntimeException("Post is already " + post.getVerificationStatus().name().toLowerCase());
        }
        
        if (approved) {
            post.setVerificationStatus(SocialWorkPost.VerificationStatus.APPROVED);
            post.setVerifiedBy(admin);
            post.setVerifiedAt(java.time.LocalDateTime.now());
            
            // Calculate and assign impact score
            int impactScore = calculateImpactScore(post.getActivityType());
            post.setImpactScoreEarned(impactScore);
            
            // Update user's impact score
            impactScoreService.addImpactScore(post.getUser().getId(), impactScore, 
                post.getActivityType().name(), postId, "Social work verification");
            
            // Update user's total social work count
            User user = post.getUser();
            user.setTotalSocialWork(user.getTotalSocialWork() + 1);
            userService.save(user);
        } else {
            // Only require rejection reason if rejecting
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new RuntimeException("Rejection reason is required when rejecting a post");
            }
            post.setVerificationStatus(SocialWorkPost.VerificationStatus.REJECTED);
            post.setVerifiedBy(admin);
            post.setVerifiedAt(java.time.LocalDateTime.now());
            post.setRejectionReason(rejectionReason.trim());
        }
        
        SocialWorkPost savedPost = postRepository.save(post);
        
        // Load media proofs explicitly
        List<com.kindcircle.model.MediaProof> mediaProofs = mediaProofRepository.findByPostId(savedPost.getId());
        if (mediaProofs == null) {
            mediaProofs = new ArrayList<>();
        }
        savedPost.setMediaProofs(mediaProofs);
        
        Long upvotes = voteRepository.countByPostIdAndVoteType(postId, Vote.VoteType.UPVOTE);
        Long downvotes = voteRepository.countByPostIdAndVoteType(postId, Vote.VoteType.DOWNVOTE);
        Long commentsCount = (long) commentRepository.findByPostIdOrderByCreatedAtDesc(postId).size();
        
        return PostResponse.fromPost(savedPost, upvotes, downvotes, commentsCount);
    }
    
    private int calculateImpactScore(SocialWorkPost.ActivityType activityType) {
        return switch (activityType) {
            case BLOOD_DONATION -> 100;
            case FEEDING -> 40;
            case WINTER_CLOTHES -> 30;
            case TREE_PLANTATION -> 20;
        };
    }
    
    @Transactional
    public void deletePost(Long postId) {
        User currentUser = getCurrentUser();
        SocialWorkPost post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Only allow users to delete their own posts
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own posts");
        }
        
        // Delete associated media files from disk
        List<MediaProof> mediaProofs = mediaProofRepository.findByPostId(postId);
        for (MediaProof proof : mediaProofs) {
            fileStorageService.deleteFile(proof.getMediaUrl());
        }
        
        // Delete all comment votes for all comments on this post before deleting comments
        // This prevents foreign key constraint violations
        // Only process top-level comments - recursion will handle all replies
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId);
        for (Comment comment : topLevelComments) {
            // Delete votes for this comment and all its replies recursively
            deleteCommentVotesRecursively(comment.getId());
        }
        
        // Delete associated media proofs, votes, and comments (cascade should handle this)
        postRepository.delete(post);
    }
    
    private void deleteCommentVotesRecursively(Long commentId) {
        // Delete votes for this comment
        commentVoteRepository.deleteByCommentId(commentId);
        
        // Find all replies to this comment and delete their votes recursively
        List<Comment> replies = commentRepository.findByParentCommentId(commentId);
        for (Comment reply : replies) {
            deleteCommentVotesRecursively(reply.getId());
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}

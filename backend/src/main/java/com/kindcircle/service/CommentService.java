package com.kindcircle.service;

import com.kindcircle.dto.CommentRequest;
import com.kindcircle.dto.CommentResponse;
import com.kindcircle.model.Comment;
import com.kindcircle.model.CommentVote;
import com.kindcircle.model.Notification;
import com.kindcircle.model.SocialWorkPost;
import com.kindcircle.model.User;
import com.kindcircle.repository.CommentRepository;
import com.kindcircle.repository.CommentVoteRepository;
import com.kindcircle.repository.SocialWorkPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private SocialWorkPostRepository postRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private CommentVoteRepository commentVoteRepository;
    
    public List<CommentResponse> getCommentsByPostId(Long postId, Long currentUserId) {
        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
        
        // Get all votes for all comments in this post
        Map<Long, Long> upvotesMap = new HashMap<>();
        Map<Long, Long> downvotesMap = new HashMap<>();
        Map<Long, String> userVotesMap = new HashMap<>();
        
        for (Comment comment : allComments) {
            long upvotes = commentVoteRepository.countByCommentIdAndVoteType(comment.getId(), CommentVote.VoteType.UPVOTE);
            long downvotes = commentVoteRepository.countByCommentIdAndVoteType(comment.getId(), CommentVote.VoteType.DOWNVOTE);
            upvotesMap.put(comment.getId(), upvotes);
            downvotesMap.put(comment.getId(), downvotes);
            
            if (currentUserId != null) {
                commentVoteRepository.findByUserIdAndCommentId(currentUserId, comment.getId())
                    .ifPresent(vote -> userVotesMap.put(comment.getId(), vote.getVoteType().name()));
            }
        }
        
        // Build comment tree
        Map<Long, CommentResponse> commentMap = new HashMap<>();
        List<CommentResponse> topLevelComments = new ArrayList<>();
        
        // Create all comment responses
        for (Comment comment : allComments) {
            CommentResponse response = CommentResponse.fromComment(
                comment,
                0L, // Will be set later
                upvotesMap.getOrDefault(comment.getId(), 0L),
                downvotesMap.getOrDefault(comment.getId(), 0L),
                userVotesMap.get(comment.getId())
            );
            commentMap.put(comment.getId(), response);
        }
        
        // Build tree structure
        for (Comment comment : allComments) {
            CommentResponse response = commentMap.get(comment.getId());
            if (comment.getParentComment() == null) {
                // Top-level comment
                topLevelComments.add(response);
            } else {
                // Reply
                CommentResponse parentResponse = commentMap.get(comment.getParentComment().getId());
                if (parentResponse != null) {
                    parentResponse.getReplies().add(response);
                }
            }
        }
        
        // Calculate replies count and sort
        for (CommentResponse response : commentMap.values()) {
            response.setRepliesCount((long) response.getReplies().size());
            // Sort replies by votes (upvotes - downvotes) descending, then by date
            response.getReplies().sort((a, b) -> {
                long scoreA = (a.getUpvotes() - a.getDownvotes());
                long scoreB = (b.getUpvotes() - b.getDownvotes());
                if (scoreA != scoreB) {
                    return Long.compare(scoreB, scoreA); // Descending
                }
                return b.getCreatedAt().compareTo(a.getCreatedAt()); // Newest first
            });
        }
        
        // Sort top-level comments by votes (upvotes - downvotes) descending, then by date
        topLevelComments.sort((a, b) -> {
            long scoreA = (a.getUpvotes() - a.getDownvotes());
            long scoreB = (b.getUpvotes() - b.getDownvotes());
            if (scoreA != scoreB) {
                return Long.compare(scoreB, scoreA); // Descending
            }
            return b.getCreatedAt().compareTo(a.getCreatedAt()); // Newest first
        });
        
        return topLevelComments;
    }
    
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request) {
        User currentUser = getCurrentUser();
        SocialWorkPost post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(currentUser);
        comment.setContent(request.getContent());
        
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }
        
        Comment savedComment = commentRepository.save(comment);
        
        // Create notification - if it's a reply, notify the parent comment author; otherwise notify post author
        if (request.getParentCommentId() != null) {
            // Reply to comment - notify parent comment author
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            if (!parentComment.getUser().getId().equals(currentUser.getId())) {
                String message = currentUser.getFullName() + " replied to your comment";
                notificationService.createNotification(
                    parentComment.getUser().getId(),
                    Notification.NotificationType.COMMENT,
                    message,
                    postId,
                    currentUser.getId()
                );
            }
        } else {
            // Top-level comment - notify post author
            if (!post.getUser().getId().equals(currentUser.getId())) {
                String message = currentUser.getFullName() + " commented on your post";
                notificationService.createNotification(
                    post.getUser().getId(),
                    Notification.NotificationType.COMMENT,
                    message,
                    postId,
                    currentUser.getId()
                );
            }
        }
        
        return CommentResponse.fromComment(savedComment, 0L, 0L, 0L, null);
    }
    
    @Transactional
    public void voteComment(Long commentId, String voteType) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        CommentVote.VoteType type = CommentVote.VoteType.valueOf(voteType);
        final boolean[] voteTypeChanged = {false};
        
        // Check if user already voted
        commentVoteRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
            .ifPresentOrElse(
                existingVote -> {
                    if (existingVote.getVoteType() == type) {
                        // Remove vote if same type clicked again
                        commentVoteRepository.delete(existingVote);
                    } else {
                        // Update vote type
                        existingVote.setVoteType(type);
                        commentVoteRepository.save(existingVote);
                        voteTypeChanged[0] = true;
                    }
                },
                () -> {
                    // Create new vote
                    CommentVote vote = new CommentVote();
                    vote.setComment(comment);
                    vote.setUser(currentUser);
                    vote.setVoteType(type);
                    commentVoteRepository.save(vote);
                    voteTypeChanged[0] = true;
                }
            );
        
        // Create notification if user is voting on someone else's comment (only for new votes or vote type changes)
        if (voteTypeChanged[0] && !comment.getUser().getId().equals(currentUser.getId())) {
            String message = type == CommentVote.VoteType.UPVOTE 
                ? currentUser.getFullName() + " upvoted your comment"
                : currentUser.getFullName() + " downvoted your comment";
            Notification.NotificationType notificationType = type == CommentVote.VoteType.UPVOTE 
                ? Notification.NotificationType.UPVOTE 
                : Notification.NotificationType.DOWNVOTE;
            notificationService.createNotification(
                comment.getUser().getId(),
                notificationType,
                message,
                comment.getPost().getId(),
                currentUser.getId()
            );
        }
    }
    
    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        // Get the post to check if current user is the post owner
        SocialWorkPost post = comment.getPost();
        boolean isCommentOwner = comment.getUser().getId().equals(currentUser.getId());
        boolean isPostOwner = post.getUser().getId().equals(currentUser.getId());
        
        if (!isCommentOwner && !isPostOwner) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }
        
        // Delete the comment (cascade will handle replies)
        commentRepository.delete(comment);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}

package com.kindcircle.dto;

import com.kindcircle.model.Comment;
import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {
    private Long id;
    private PostResponse.UserInfo user;
    private String content;
    private LocalDateTime createdAt;
    private Long repliesCount;
    private Long upvotes;
    private Long downvotes;
    private String userVote; // "UPVOTE", "DOWNVOTE", or null
    private Long parentCommentId;
    private List<CommentResponse> replies;
    
    public CommentResponse() {}
    
    public CommentResponse(Long id, PostResponse.UserInfo user, String content, LocalDateTime createdAt, Long repliesCount) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.createdAt = createdAt;
        this.repliesCount = repliesCount;
        this.replies = new java.util.ArrayList<>();
    }
    
    public static CommentResponse fromComment(Comment comment, Long repliesCount, Long upvotes, Long downvotes, String userVote) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setUser(PostResponse.UserInfo.fromUser(comment.getUser()));
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setRepliesCount(repliesCount);
        response.setUpvotes(upvotes);
        response.setDownvotes(downvotes);
        response.setUserVote(userVote);
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }
        response.setReplies(new java.util.ArrayList<>());
        return response;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public PostResponse.UserInfo getUser() { return user; }
    public void setUser(PostResponse.UserInfo user) { this.user = user; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Long getRepliesCount() { return repliesCount; }
    public void setRepliesCount(Long repliesCount) { this.repliesCount = repliesCount; }
    
    public Long getUpvotes() { return upvotes; }
    public void setUpvotes(Long upvotes) { this.upvotes = upvotes; }
    
    public Long getDownvotes() { return downvotes; }
    public void setDownvotes(Long downvotes) { this.downvotes = downvotes; }
    
    public String getUserVote() { return userVote; }
    public void setUserVote(String userVote) { this.userVote = userVote; }
    
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    
    public List<CommentResponse> getReplies() { return replies; }
    public void setReplies(List<CommentResponse> replies) { this.replies = replies; }
}

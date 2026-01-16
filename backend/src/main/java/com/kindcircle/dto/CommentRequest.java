package com.kindcircle.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;
    
    private Long parentCommentId; // For nested comments
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
}

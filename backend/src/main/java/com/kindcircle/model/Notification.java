package com.kindcircle.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "related_post_id")
    private Long relatedPostId;

    @Column(name = "related_campaign_id")
    private Long relatedCampaignId;
    
    @Column(name = "related_user_id")
    private Long relatedUserId;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum NotificationType {
        UPVOTE, DOWNVOTE, COMMENT, POST_APPROVED, POST_REJECTED,
        POST_PENDING_APPROVAL, CAMPAIGN_PENDING_APPROVAL
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Long getRelatedPostId() { return relatedPostId; }
    public void setRelatedPostId(Long relatedPostId) { this.relatedPostId = relatedPostId; }

    public Long getRelatedCampaignId() { return relatedCampaignId; }
    public void setRelatedCampaignId(Long relatedCampaignId) { this.relatedCampaignId = relatedCampaignId; }
    
    public Long getRelatedUserId() { return relatedUserId; }
    public void setRelatedUserId(Long relatedUserId) { this.relatedUserId = relatedUserId; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

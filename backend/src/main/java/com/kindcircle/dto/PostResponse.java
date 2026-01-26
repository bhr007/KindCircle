package com.kindcircle.dto;

import com.kindcircle.model.SocialWorkPost;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostResponse {
    private Long id;
    private UserInfo user;
    private String activityType;
    private String title;
    private String description;
    private String location;
    private LocalDate activityDate;
    private Integer impactScoreEarned;
    private String verificationStatus;
    private LocalDateTime createdAt;
    private List<MediaInfo> mediaProofs = new ArrayList<>();
    private Long upvotes;
    private Long downvotes;
    private Long commentsCount;
    private String userVote; // "UPVOTE", "DOWNVOTE", or null
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
    
    public Integer getImpactScoreEarned() { return impactScoreEarned; }
    public void setImpactScoreEarned(Integer impactScoreEarned) { this.impactScoreEarned = impactScoreEarned; }
    
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<MediaInfo> getMediaProofs() { return mediaProofs; }
    public void setMediaProofs(List<MediaInfo> mediaProofs) { this.mediaProofs = mediaProofs; }
    
    public Long getUpvotes() { return upvotes; }
    public void setUpvotes(Long upvotes) { this.upvotes = upvotes; }
    
    public Long getDownvotes() { return downvotes; }
    public void setDownvotes(Long downvotes) { this.downvotes = downvotes; }
    
    public Long getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Long commentsCount) { this.commentsCount = commentsCount; }
    
    public String getUserVote() { return userVote; }
    public void setUserVote(String userVote) { this.userVote = userVote; }
    
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String profilePhotoUrl;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getProfilePhotoUrl() { return profilePhotoUrl; }
        public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
        
        public static UserInfo fromUser(com.kindcircle.model.User user) {
            UserInfo info = new UserInfo();
            info.setId(user.getId());
            info.setUsername(user.getUsername());
            info.setFullName(user.getFullName());
            info.setProfilePhotoUrl(user.getProfilePhotoUrl());
            return info;
        }
    }
    
    public static class MediaInfo {
        private Long id;
        private String mediaType;
        private String mediaUrl;
        private String thumbnailUrl;
        private String mimeType;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
        
        public String getMediaUrl() { return mediaUrl; }
        public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
        
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        
        public static MediaInfo fromMediaProof(com.kindcircle.model.MediaProof proof) {
            MediaInfo info = new MediaInfo();
            info.setId(proof.getId());
            info.setMediaType(proof.getMediaType().name());
            info.setMediaUrl(proof.getMediaUrl());
            info.setThumbnailUrl(proof.getThumbnailUrl());
            info.setMimeType(proof.getMimeType());
            return info;
        }
    }
    
    public static PostResponse fromPost(SocialWorkPost post, Long upvotes, Long downvotes, Long commentsCount, String userVote) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setUser(UserInfo.fromUser(post.getUser()));
        response.setActivityType(post.getActivityType().name());
        response.setTitle(post.getTitle());
        response.setDescription(post.getDescription());
        response.setLocation(post.getLocation());
        response.setActivityDate(post.getActivityDate());
        response.setImpactScoreEarned(post.getImpactScoreEarned());
        response.setVerificationStatus(post.getVerificationStatus().name());
        response.setCreatedAt(post.getCreatedAt());
        
        // Handle media proofs - check both null and empty
        List<com.kindcircle.model.MediaProof> mediaProofs = post.getMediaProofs();
        
        if (mediaProofs != null && !mediaProofs.isEmpty()) {
            List<MediaInfo> mediaInfoList = mediaProofs.stream()
                .filter(proof -> proof.getMediaUrl() != null && !proof.getMediaUrl().trim().isEmpty())
                .map(MediaInfo::fromMediaProof)
                .collect(Collectors.toList());
            response.setMediaProofs(mediaInfoList);
        } else {
            response.setMediaProofs(new ArrayList<>());
        }
        
        response.setUpvotes(upvotes);
        response.setDownvotes(downvotes);
        response.setCommentsCount(commentsCount);
        response.setUserVote(userVote);
        
        return response;
    }
}

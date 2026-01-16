package com.kindcircle.dto;

import com.kindcircle.model.BloodRequest;
import java.time.LocalDateTime;

public class BloodRequestResponse {
    private Long id;
    private UserInfo requester;
    private String bloodGroup;
    private Integer bagsRequired;
    private String hospitalName;
    private String hospitalAddress;
    private String city;
    private String urgencyLevel;
    private String description;
    private String status;
    private Integer impactScoreRequired;
    private UserInfo matchedDonor;
    private LocalDateTime matchedAt;
    private Boolean locationSharingEnabled;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    
    public static BloodRequestResponse fromBloodRequest(BloodRequest request) {
        BloodRequestResponse response = new BloodRequestResponse();
        response.setId(request.getId());
        response.setRequester(UserInfo.fromUser(request.getRequester()));
        response.setBloodGroup(request.getBloodGroup());
        response.setBagsRequired(request.getBagsRequired());
        response.setHospitalName(request.getHospitalName());
        response.setHospitalAddress(request.getHospitalAddress());
        response.setCity(request.getCity());
        response.setUrgencyLevel(request.getUrgencyLevel().name());
        response.setDescription(request.getDescription());
        response.setStatus(request.getStatus().name());
        response.setImpactScoreRequired(request.getImpactScoreRequired());
        if (request.getMatchedDonor() != null) {
            response.setMatchedDonor(UserInfo.fromUser(request.getMatchedDonor()));
        }
        response.setMatchedAt(request.getMatchedAt());
        response.setLocationSharingEnabled(request.getLocationSharingEnabled());
        response.setCompletedAt(request.getCompletedAt());
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserInfo getRequester() { return requester; }
    public void setRequester(UserInfo requester) { this.requester = requester; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public Integer getBagsRequired() { return bagsRequired; }
    public void setBagsRequired(Integer bagsRequired) { this.bagsRequired = bagsRequired; }
    
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    
    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getImpactScoreRequired() { return impactScoreRequired; }
    public void setImpactScoreRequired(Integer impactScoreRequired) { this.impactScoreRequired = impactScoreRequired; }
    
    public UserInfo getMatchedDonor() { return matchedDonor; }
    public void setMatchedDonor(UserInfo matchedDonor) { this.matchedDonor = matchedDonor; }
    
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
    
    public Boolean getLocationSharingEnabled() { return locationSharingEnabled; }
    public void setLocationSharingEnabled(Boolean locationSharingEnabled) { this.locationSharingEnabled = locationSharingEnabled; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Reuse UserInfo from PostResponse
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String profilePhotoUrl;
        private Integer currentImpactScore;
        
        public static UserInfo fromUser(com.kindcircle.model.User user) {
            UserInfo info = new UserInfo();
            info.setId(user.getId());
            info.setUsername(user.getUsername());
            info.setFullName(user.getFullName());
            info.setProfilePhotoUrl(user.getProfilePhotoUrl());
            info.setCurrentImpactScore(user.getCurrentImpactScore());
            return info;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getProfilePhotoUrl() { return profilePhotoUrl; }
        public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
        
        public Integer getCurrentImpactScore() { return currentImpactScore; }
        public void setCurrentImpactScore(Integer currentImpactScore) { this.currentImpactScore = currentImpactScore; }
    }
}

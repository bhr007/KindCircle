package com.kindcircle.dto;

import com.kindcircle.model.BloodRequest;
import com.kindcircle.model.Chat;
import java.time.LocalDateTime;

public class ConversationResponse {
    private Long requestId;
    private BloodRequestResponse requestInfo;
    private UserInfo otherUser;
    private Chat lastMessage;
    private Long unreadCount;
    private Boolean locationSharingEnabled;
    private Boolean isDonor;
    private LocalDateTime lastActivityAt;
    private DonationInfo donationInfo;
    
    public static ConversationResponse fromRequestAndLastMessage(
            BloodRequest request,
            Chat lastMessage,
            Long unreadCount,
            Long currentUserId) {
        ConversationResponse response = new ConversationResponse();
        response.setRequestId(request.getId());
        response.setRequestInfo(BloodRequestResponse.fromBloodRequest(request));
        
        // Determine the other user
        boolean isRequester = request.getRequester().getId().equals(currentUserId);
        response.setIsDonor(!isRequester);
        
        if (isRequester) {
            // Current user is requester, other user is donor (if matched)
            if (request.getMatchedDonor() != null) {
                response.setOtherUser(UserInfo.fromUser(request.getMatchedDonor()));
            } else {
                // No matched donor yet, but there are messages - find the other participant
                if (lastMessage != null) {
                    if (!lastMessage.getSender().getId().equals(currentUserId)) {
                        response.setOtherUser(UserInfo.fromUser(lastMessage.getSender()));
                    } else {
                        response.setOtherUser(UserInfo.fromUser(lastMessage.getReceiver()));
                    }
                }
            }
        } else {
            // Current user is donor, other user is requester
            response.setOtherUser(UserInfo.fromUser(request.getRequester()));
        }
        
        response.setLastMessage(lastMessage);
        response.setUnreadCount(unreadCount != null ? unreadCount : 0L);
        response.setLocationSharingEnabled(request.getLocationSharingEnabled());
        response.setLastActivityAt(lastMessage != null ? lastMessage.getCreatedAt() : request.getCreatedAt());
        
        return response;
    }
    
    // Getters and Setters
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    
    public BloodRequestResponse getRequestInfo() { return requestInfo; }
    public void setRequestInfo(BloodRequestResponse requestInfo) { this.requestInfo = requestInfo; }
    
    public UserInfo getOtherUser() { return otherUser; }
    public void setOtherUser(UserInfo otherUser) { this.otherUser = otherUser; }
    
    public Chat getLastMessage() { return lastMessage; }
    public void setLastMessage(Chat lastMessage) { this.lastMessage = lastMessage; }
    
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
    
    public Boolean getLocationSharingEnabled() { return locationSharingEnabled; }
    public void setLocationSharingEnabled(Boolean locationSharingEnabled) { this.locationSharingEnabled = locationSharingEnabled; }
    
    public Boolean getIsDonor() { return isDonor; }
    public void setIsDonor(Boolean isDonor) { this.isDonor = isDonor; }
    
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    
    public DonationInfo getDonationInfo() { return donationInfo; }
    public void setDonationInfo(DonationInfo donationInfo) { this.donationInfo = donationInfo; }
    
    // UserInfo class - same structure as BloodRequestResponse.UserInfo
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String profilePhotoUrl;
        private Integer currentImpactScore;
        
        public static UserInfo fromUser(com.kindcircle.model.User user) {
            if (user == null) return null;
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
    
    // DonationInfo class
    public static class DonationInfo {
        private Long donationId;
        private String status;
        private Boolean donorConfirmed;
        private Boolean requesterConfirmed;
        
        public static DonationInfo fromDonation(com.kindcircle.model.BloodDonation donation) {
            if (donation == null) return null;
            DonationInfo info = new DonationInfo();
            info.setDonationId(donation.getId());
            info.setStatus(donation.getStatus().name());
            info.setDonorConfirmed(donation.getDonorConfirmed());
            info.setRequesterConfirmed(donation.getRequesterConfirmed());
            return info;
        }
        
        // Getters and Setters
        public Long getDonationId() { return donationId; }
        public void setDonationId(Long donationId) { this.donationId = donationId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Boolean getDonorConfirmed() { return donorConfirmed; }
        public void setDonorConfirmed(Boolean donorConfirmed) { this.donorConfirmed = donorConfirmed; }
        
        public Boolean getRequesterConfirmed() { return requesterConfirmed; }
        public void setRequesterConfirmed(Boolean requesterConfirmed) { this.requesterConfirmed = requesterConfirmed; }
    }
}

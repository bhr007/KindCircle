package com.kindcircle.dto;

import com.kindcircle.model.User;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean phoneVerified;
    private Boolean emailVerified;
    private String fullName;
    private String bloodGroup;
    private String city;
    private String location;
    private String profilePhotoUrl;
    private Integer currentImpactScore;
    private Integer totalBloodDonations;
    private java.time.LocalDate lastDonationDate;
    private Integer totalSocialWork;
    private Integer upvotesReceived;
    private Integer downvotesReceived;
    private Integer responseCount; // Number of emergency requests responded to
    private Integer successfulDonations; // Number of completed donations as donor
    private Double trustRatio; // Calculated trust ratio

    public static UserProfileResponse fromUser(User user, Integer responseCount, Integer successfulDonations) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setPhoneVerified(user.getPhoneVerified());
        response.setEmailVerified(user.getEmailVerified());
        response.setFullName(user.getFullName());
        response.setBloodGroup(user.getBloodGroup());
        response.setCity(user.getCity());
        response.setLocation(user.getLocation());
        response.setProfilePhotoUrl(user.getProfilePhotoUrl());
        response.setCurrentImpactScore(user.getCurrentImpactScore());
        response.setTotalBloodDonations(user.getTotalBloodDonations());
        response.setLastDonationDate(user.getLastDonationDate());
        response.setTotalSocialWork(user.getTotalSocialWork());
        response.setUpvotesReceived(user.getUpvotesReceived());
        response.setDownvotesReceived(user.getDownvotesReceived());
        response.setResponseCount(responseCount != null ? responseCount : 0);
        response.setSuccessfulDonations(successfulDonations != null ? successfulDonations : 0);
        
        // Calculate trust ratio
        int totalVotes = user.getUpvotesReceived() + user.getDownvotesReceived();
        double voteRatio = totalVotes > 0 
            ? ((double) user.getUpvotesReceived() / totalVotes) * 100.0 
            : 0.0;
        
        // Factor in responses and donations
        // Base trust: 50% from votes, 30% from successful donations, 20% from responses
        double donationScore = Math.min(100.0, (response.getSuccessfulDonations() * 10.0)); // Max 100 points
        double responseScore = Math.min(100.0, (response.getResponseCount() * 5.0)); // Max 100 points
        
        double trustRatio = (voteRatio * 0.5) + (donationScore * 0.3) + (responseScore * 0.2);
        response.setTrustRatio(Math.round(trustRatio * 10.0) / 10.0); // Round to 1 decimal place
        
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public Integer getCurrentImpactScore() { return currentImpactScore; }
    public void setCurrentImpactScore(Integer currentImpactScore) { this.currentImpactScore = currentImpactScore; }

    public Integer getTotalBloodDonations() { return totalBloodDonations; }
    public void setTotalBloodDonations(Integer totalBloodDonations) { this.totalBloodDonations = totalBloodDonations; }

    public java.time.LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(java.time.LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public Integer getTotalSocialWork() { return totalSocialWork; }
    public void setTotalSocialWork(Integer totalSocialWork) { this.totalSocialWork = totalSocialWork; }

    public Integer getUpvotesReceived() { return upvotesReceived; }
    public void setUpvotesReceived(Integer upvotesReceived) { this.upvotesReceived = upvotesReceived; }

    public Integer getDownvotesReceived() { return downvotesReceived; }
    public void setDownvotesReceived(Integer downvotesReceived) { this.downvotesReceived = downvotesReceived; }

    public Integer getResponseCount() { return responseCount; }
    public void setResponseCount(Integer responseCount) { this.responseCount = responseCount; }

    public Integer getSuccessfulDonations() { return successfulDonations; }
    public void setSuccessfulDonations(Integer successfulDonations) { this.successfulDonations = successfulDonations; }

    public Double getTrustRatio() { return trustRatio; }
    public void setTrustRatio(Double trustRatio) { this.trustRatio = trustRatio; }
}

package com.kindcircle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class PostRequest {
    @NotNull(message = "Activity type is required")
    private String activityType; // BLOOD_DONATION, FEEDING, WINTER_CLOTHES, TREE_PLANTATION
    
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String location;
    
    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;
    
    private List<String> mediaUrls; // URLs of uploaded media files
    
    // Getters and Setters
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
    
    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }
}

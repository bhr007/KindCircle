package com.kindcircle.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @Column(name = "blood_group", nullable = false, length = 5)
    private String bloodGroup;
    
    @Column(name = "bags_required", nullable = false)
    private Integer bagsRequired = 1;
    
    @Column(name = "hospital_name", nullable = false, length = 200)
    private String hospitalName;
    
    @Column(name = "hospital_address", nullable = false, length = 500)
    private String hospitalAddress;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level")
    private UrgencyLevel urgencyLevel = UrgencyLevel.MEDIUM;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.OPEN;
    
    @Column(name = "impact_score_required", nullable = false)
    private Integer impactScoreRequired;
    
    @ManyToOne
    @JoinColumn(name = "matched_donor_id")
    private User matchedDonor;
    
    @Column(name = "matched_at")
    private LocalDateTime matchedAt;
    
    @Column(name = "location_sharing_enabled")
    private Boolean locationSharingEnabled = false;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum UrgencyLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum Status {
        OPEN, MATCHED, IN_PROGRESS, COMPLETED, CANCELLED
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    
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
    
    public UrgencyLevel getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(UrgencyLevel urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public Integer getImpactScoreRequired() { return impactScoreRequired; }
    public void setImpactScoreRequired(Integer impactScoreRequired) { this.impactScoreRequired = impactScoreRequired; }
    
    public User getMatchedDonor() { return matchedDonor; }
    public void setMatchedDonor(User matchedDonor) { this.matchedDonor = matchedDonor; }
    
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
    
    public Boolean getLocationSharingEnabled() { return locationSharingEnabled; }
    public void setLocationSharingEnabled(Boolean locationSharingEnabled) { this.locationSharingEnabled = locationSharingEnabled; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

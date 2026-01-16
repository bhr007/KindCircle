package com.kindcircle.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_sessions")
public class LocationSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private BloodRequest request;
    
    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;
    
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @Column(name = "donor_location_lat", precision = 10, scale = 8)
    private BigDecimal donorLocationLat;
    
    @Column(name = "donor_location_lng", precision = 11, scale = 8)
    private BigDecimal donorLocationLng;
    
    @Column(name = "requester_location_lat", precision = 10, scale = 8)
    private BigDecimal requesterLocationLat;
    
    @Column(name = "requester_location_lng", precision = 11, scale = 8)
    private BigDecimal requesterLocationLng;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BloodRequest getRequest() { return request; }
    public void setRequest(BloodRequest request) { this.request = request; }
    
    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }
    
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    
    public BigDecimal getDonorLocationLat() { return donorLocationLat; }
    public void setDonorLocationLat(BigDecimal donorLocationLat) { this.donorLocationLat = donorLocationLat; }
    
    public BigDecimal getDonorLocationLng() { return donorLocationLng; }
    public void setDonorLocationLng(BigDecimal donorLocationLng) { this.donorLocationLng = donorLocationLng; }
    
    public BigDecimal getRequesterLocationLat() { return requesterLocationLat; }
    public void setRequesterLocationLat(BigDecimal requesterLocationLat) { this.requesterLocationLat = requesterLocationLat; }
    
    public BigDecimal getRequesterLocationLng() { return requesterLocationLng; }
    public void setRequesterLocationLng(BigDecimal requesterLocationLng) { this.requesterLocationLng = requesterLocationLng; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.kindcircle.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_donations")
public class BloodDonation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "request_id")
    private BloodRequest request;
    
    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;
    
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @Column(name = "blood_group", nullable = false, length = 5)
    private String bloodGroup;
    
    @Column(name = "donation_date", nullable = false)
    private LocalDate donationDate;
    
    @Column(name = "hospital_name", nullable = false, length = 200)
    private String hospitalName;
    
    @Column(name = "impact_score_donor")
    private Integer impactScoreDonor = 100;
    
    @Column(name = "impact_score_requester")
    private Integer impactScoreRequester = -100;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING_CONFIRMATION;
    
    @Column(name = "donor_confirmed")
    private Boolean donorConfirmed = false;
    
    @Column(name = "requester_confirmed")
    private Boolean requesterConfirmed = false;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum Status {
        PENDING_CONFIRMATION, CONFIRMED, COMPLETED
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BloodRequest getRequest() { return request; }
    public void setRequest(BloodRequest request) { this.request = request; }
    
    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }
    
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public LocalDate getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }
    
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    
    public Integer getImpactScoreDonor() { return impactScoreDonor; }
    public void setImpactScoreDonor(Integer impactScoreDonor) { this.impactScoreDonor = impactScoreDonor; }
    
    public Integer getImpactScoreRequester() { return impactScoreRequester; }
    public void setImpactScoreRequester(Integer impactScoreRequester) { this.impactScoreRequester = impactScoreRequester; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public Boolean getDonorConfirmed() { return donorConfirmed; }
    public void setDonorConfirmed(Boolean donorConfirmed) { this.donorConfirmed = donorConfirmed; }
    
    public Boolean getRequesterConfirmed() { return requesterConfirmed; }
    public void setRequesterConfirmed(Boolean requesterConfirmed) { this.requesterConfirmed = requesterConfirmed; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

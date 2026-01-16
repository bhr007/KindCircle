package com.kindcircle.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String password;
    
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    
    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "blood_group")
    private String bloodGroup;
    
    private String city;
    private String location;
    
    @Column(name = "profile_photo_url", columnDefinition = "TEXT")
    private String profilePhotoUrl;
    
    @Column(name = "current_impact_score")
    private Integer currentImpactScore = 0;
    
    @Column(name = "total_blood_donations")
    private Integer totalBloodDonations = 0;
    
    @Column(name = "last_donation_date")
    private java.time.LocalDate lastDonationDate;
    
    @Column(name = "total_social_work")
    private Integer totalSocialWork = 0;
    
    @Column(name = "upvotes_received")
    private Integer upvotesReceived = 0;
    
    @Column(name = "downvotes_received")
    private Integer downvotesReceived = 0;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }
    
    public LocalDateTime getEmailVerificationTokenExpiry() {
        return emailVerificationTokenExpiry;
    }

    public void setEmailVerificationTokenExpiry(LocalDateTime emailVerificationTokenExpiry) {
        this.emailVerificationTokenExpiry = emailVerificationTokenExpiry;
    }
    
    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }
    
    public LocalDateTime getPasswordResetTokenExpiry() {
        return passwordResetTokenExpiry;
    }

    public void setPasswordResetTokenExpiry(LocalDateTime passwordResetTokenExpiry) {
        this.passwordResetTokenExpiry = passwordResetTokenExpiry;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public Integer getCurrentImpactScore() {
        return currentImpactScore;
    }

    public void setCurrentImpactScore(Integer currentImpactScore) {
        this.currentImpactScore = currentImpactScore;
    }

    public Integer getTotalBloodDonations() {
        return totalBloodDonations;
    }

    public void setTotalBloodDonations(Integer totalBloodDonations) {
        this.totalBloodDonations = totalBloodDonations;
    }

    public java.time.LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(java.time.LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public Integer getTotalSocialWork() {
        return totalSocialWork;
    }

    public void setTotalSocialWork(Integer totalSocialWork) {
        this.totalSocialWork = totalSocialWork;
    }

    public Integer getUpvotesReceived() {
        return upvotesReceived;
    }

    public void setUpvotesReceived(Integer upvotesReceived) {
        this.upvotesReceived = upvotesReceived;
    }

    public Integer getDownvotesReceived() {
        return downvotesReceived;
    }

    public void setDownvotesReceived(Integer downvotesReceived) {
        this.downvotesReceived = downvotesReceived;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public enum Role {
        USER, ADMIN
    }
}

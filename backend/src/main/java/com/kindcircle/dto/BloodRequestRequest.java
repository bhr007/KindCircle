package com.kindcircle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BloodRequestRequest {
    @NotBlank(message = "Blood group is required")
    private String bloodGroup;
    
    @NotNull(message = "Number of bags is required")
    @Min(value = 1, message = "At least 1 bag is required")
    private Integer bagsRequired;
    
    @NotBlank(message = "Hospital name is required")
    private String hospitalName;
    
    @NotBlank(message = "Hospital address is required")
    private String hospitalAddress;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotNull(message = "Urgency level is required")
    private String urgencyLevel;
    
    private String description;
    
    // Getters and Setters
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
}

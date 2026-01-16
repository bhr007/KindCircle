package com.kindcircle.controller;

import com.kindcircle.dto.BloodRequestRequest;
import com.kindcircle.dto.BloodRequestResponse;
import com.kindcircle.model.BloodRequest;
import com.kindcircle.model.Chat;
import com.kindcircle.model.LocationSession;
import com.kindcircle.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emergency")
@CrossOrigin(origins = "http://localhost:3000")
public class EmergencyController {
    
    @Autowired
    private BloodRequestService bloodRequestService;
    
    @Autowired
    private com.kindcircle.service.UserService userService;
    
    @GetMapping
    public ResponseEntity<List<BloodRequestResponse>> getOpenRequests() {
        List<BloodRequest> requests = bloodRequestService.getOpenRequests();
        List<BloodRequestResponse> responses = requests.stream()
            .map(BloodRequestResponse::fromBloodRequest)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping
    public ResponseEntity<BloodRequestResponse> createRequest(@Valid @RequestBody BloodRequestRequest request) {
        BloodRequest bloodRequest = new BloodRequest();
        bloodRequest.setBloodGroup(request.getBloodGroup());
        bloodRequest.setBagsRequired(request.getBagsRequired());
        bloodRequest.setHospitalName(request.getHospitalName());
        bloodRequest.setHospitalAddress(request.getHospitalAddress());
        bloodRequest.setCity(request.getCity());
        bloodRequest.setUrgencyLevel(BloodRequest.UrgencyLevel.valueOf(request.getUrgencyLevel().toUpperCase()));
        bloodRequest.setDescription(request.getDescription());
        
        BloodRequest saved = bloodRequestService.createRequest(bloodRequest);
        return ResponseEntity.ok(BloodRequestResponse.fromBloodRequest(saved));
    }
    
    @PostMapping("/{requestId}/respond")
    public ResponseEntity<Map<String, String>> respondToRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            message = "I can help with this blood request.";
        }
        bloodRequestService.respondToRequest(requestId, message);
        return ResponseEntity.ok(Map.of("message", "Response sent successfully"));
    }
    
    @PostMapping("/{requestId}/confirm-match/{donorId}")
    public ResponseEntity<Map<String, String>> confirmMatch(
            @PathVariable Long requestId,
            @PathVariable Long donorId) {
        bloodRequestService.confirmMatch(requestId, donorId);
        return ResponseEntity.ok(Map.of("message", "Match confirmed"));
    }
    
    @PostMapping("/donations/{donationId}/confirm")
    public ResponseEntity<Map<String, String>> confirmDonation(
            @PathVariable Long donationId,
            @RequestBody Map<String, Boolean> request) {
        boolean isDonor = request.getOrDefault("isDonor", false);
        bloodRequestService.confirmDonation(donationId, isDonor);
        return ResponseEntity.ok(Map.of("message", "Donation confirmed"));
    }
    
    @PostMapping("/donations/{donationId}/complete")
    public ResponseEntity<Map<String, String>> completeDonation(@PathVariable Long donationId) {
        bloodRequestService.completeDonation(donationId);
        return ResponseEntity.ok(Map.of("message", "Donation completed successfully"));
    }
    
    @GetMapping("/{requestId}/chat")
    public ResponseEntity<List<Chat>> getChatMessages(@PathVariable Long requestId) {
        return ResponseEntity.ok(bloodRequestService.getChatMessages(requestId));
    }
    
    @PostMapping("/{requestId}/chat")
    public ResponseEntity<Chat> sendMessage(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }
        return ResponseEntity.ok(bloodRequestService.sendMessage(requestId, message));
    }
    
    @PostMapping("/{requestId}/location")
    public ResponseEntity<LocationSession> updateLocation(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> request) {
        Double lat = Double.parseDouble(request.get("lat").toString());
        Double lng = Double.parseDouble(request.get("lng").toString());
        Boolean isDonor = (Boolean) request.getOrDefault("isDonor", false);
        return ResponseEntity.ok(bloodRequestService.updateLocation(requestId, lat, lng, isDonor));
    }
    
    @GetMapping("/{requestId}/location")
    public ResponseEntity<LocationSession> getLocation(@PathVariable Long requestId) {
        return ResponseEntity.ok(bloodRequestService.getLocationSession(requestId));
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<List<BloodRequestResponse>> getMyRequests() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        List<BloodRequest> requests = bloodRequestService.getUserRequests(user.getId());
        List<BloodRequestResponse> responses = requests.stream()
            .map(BloodRequestResponse::fromBloodRequest)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/my-donations")
    public ResponseEntity<List<com.kindcircle.model.BloodDonation>> getMyDonations() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        return ResponseEntity.ok(bloodRequestService.getUserDonations(user.getId()));
    }
    
    @GetMapping("/{requestId}/donation")
    public ResponseEntity<com.kindcircle.model.BloodDonation> getDonationByRequestId(@PathVariable Long requestId) {
        return ResponseEntity.ok(bloodRequestService.getDonationByRequestId(requestId));
    }
    
    @GetMapping("/inbox/conversations")
    public ResponseEntity<List<com.kindcircle.dto.ConversationResponse>> getMyConversations() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        return ResponseEntity.ok(bloodRequestService.getUserConversations(user.getId()));
    }
    
    @GetMapping("/inbox/{requestId}/messages")
    public ResponseEntity<List<com.kindcircle.model.Chat>> getConversationMessages(@PathVariable Long requestId) {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        return ResponseEntity.ok(bloodRequestService.getChatMessagesForUser(requestId, user.getId()));
    }
    
    @PostMapping("/inbox/{requestId}/mark-read")
    public ResponseEntity<Map<String, String>> markMessagesAsRead(@PathVariable Long requestId) {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        bloodRequestService.markMessagesAsRead(requestId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }
}

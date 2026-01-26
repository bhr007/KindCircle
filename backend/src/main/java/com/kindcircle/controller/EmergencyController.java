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
            @RequestBody Map<String, Object> body) {
        String message = body.get("message") != null ? body.get("message").toString() : null;
        Long receiverId = null;
        if (body.containsKey("receiverId") && body.get("receiverId") != null)
            receiverId = Long.valueOf(body.get("receiverId").toString());
        if (message == null || message.trim().isEmpty())
            throw new RuntimeException("Message cannot be empty");
        return ResponseEntity.ok(bloodRequestService.sendMessage(requestId, message, receiverId));
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
    public ResponseEntity<List<com.kindcircle.model.Chat>> getConversationMessages(
            @PathVariable Long requestId,
            @RequestParam(required = false) Long otherUserId) {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        return ResponseEntity.ok(bloodRequestService.getChatMessagesForUser(requestId, user.getId(), otherUserId));
    }
    
    @PostMapping("/inbox/{requestId}/mark-read")
    public ResponseEntity<Map<String, String>> markMessagesAsRead(
            @PathVariable Long requestId,
            @RequestParam(required = false) Long otherUserId) {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        bloodRequestService.markMessagesAsRead(requestId, user.getId(), otherUserId);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }
    
    @GetMapping("/{requestId}/responders")
    public ResponseEntity<List<com.kindcircle.dto.ConversationResponse.UserInfo>> getResponders(@PathVariable Long requestId) {
        List<com.kindcircle.model.User> responders = bloodRequestService.getRespondersForRequest(requestId);
        List<com.kindcircle.dto.ConversationResponse.UserInfo> responderInfos = responders.stream()
            .map(com.kindcircle.dto.ConversationResponse.UserInfo::fromUser)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responderInfos);
    }
    
    @PostMapping("/{requestId}/mark-managed")
    public ResponseEntity<Map<String, String>> markAsManaged(@PathVariable Long requestId) {
        bloodRequestService.markAsManaged(requestId);
        return ResponseEntity.ok(Map.of("message", "Blood request marked as managed"));
    }
    
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Map<String, String>> deleteRequest(@PathVariable Long requestId) {
        bloodRequestService.deleteRequest(requestId);
        return ResponseEntity.ok(Map.of("message", "Blood request deleted successfully"));
    }
    
    @DeleteMapping("/inbox/messages/{messageId}")
    public ResponseEntity<Map<String, String>> deleteChatMessage(@PathVariable Long messageId) {
        bloodRequestService.deleteChatMessage(messageId);
        return ResponseEntity.ok(Map.of("message", "Message deleted successfully"));
    }
    
    @PostMapping("/inbox/{requestId}/archive")
    public ResponseEntity<Map<String, String>> archiveConversation(
            @PathVariable Long requestId,
            @RequestParam(required = false) Long otherUserId) {
        bloodRequestService.archiveConversation(requestId, otherUserId);
        return ResponseEntity.ok(Map.of("message", "Conversation archived successfully"));
    }
    
    @DeleteMapping("/inbox/{requestId}/conversation")
    public ResponseEntity<Map<String, String>> deleteConversation(
            @PathVariable Long requestId,
            @RequestParam(required = false) Long otherUserId) {
        bloodRequestService.deleteConversation(requestId, otherUserId);
        return ResponseEntity.ok(Map.of("message", "Conversation deleted successfully"));
    }
    
    @GetMapping("/inbox/archived")
    public ResponseEntity<List<com.kindcircle.dto.ConversationResponse>> getArchivedConversations() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.kindcircle.model.User user = userService.findByUsername(auth.getName());
        return ResponseEntity.ok(bloodRequestService.getArchivedConversations(user.getId()));
    }
}

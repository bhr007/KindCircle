package com.kindcircle.service;

import com.kindcircle.model.*;
import com.kindcircle.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class BloodRequestService {
    
    @Autowired
    private BloodRequestRepository bloodRequestRepository;
    
    @Autowired
    private BloodDonationRepository bloodDonationRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ImpactScoreService impactScoreService;
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private LocationSessionRepository locationSessionRepository;
    
    public List<BloodRequest> getOpenRequests() {
        User currentUser = getCurrentUser();
        String userBloodGroup = currentUser.getBloodGroup();
        
        // OPEN, MATCHED, IN_PROGRESS — hide when COMPLETED or MANAGED
        List<BloodRequest> allOpenRequests = bloodRequestRepository.findByStatusInOrderByCreatedAtDesc(
            Arrays.asList(BloodRequest.Status.OPEN, BloodRequest.Status.MATCHED, BloodRequest.Status.IN_PROGRESS));
        
        return allOpenRequests.stream()
            .filter(request -> {
                // Exclude MANAGED requests from the feed
                if (request.getStatus() == BloodRequest.Status.MANAGED) return false;
                if (request.getRequester().getId().equals(currentUser.getId())) return true;
                if (userBloodGroup != null && !userBloodGroup.trim().isEmpty())
                    return request.getBloodGroup().equalsIgnoreCase(userBloodGroup);
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Transactional
    public BloodRequest createRequest(BloodRequest request) {
        User requester = getCurrentUser();
        
        // Validate impact score (must have >= 100 per bag)
        int requiredScore = request.getBagsRequired() * 100;
        if (requester.getCurrentImpactScore() < requiredScore) {
            throw new RuntimeException("Insufficient Impact Score. Required: " + requiredScore + ", Current: " + requester.getCurrentImpactScore());
        }
        
        request.setRequester(requester);
        request.setImpactScoreRequired(requiredScore);
        request.setStatus(BloodRequest.Status.OPEN);
        
        return bloodRequestRepository.save(request);
    }
    
    @Transactional
    public Chat respondToRequest(Long requestId, String message) {
        User donor = getCurrentUser();
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        if (request.getStatus() != BloodRequest.Status.OPEN) {
            throw new RuntimeException("This request is no longer open");
        }
        
        // Check if donor's blood group matches the request
        String donorBloodGroup = donor.getBloodGroup();
        if (donorBloodGroup == null || donorBloodGroup.trim().isEmpty()) {
            throw new RuntimeException("Please set your blood group in your profile before responding to requests");
        }
        
        if (!donorBloodGroup.equalsIgnoreCase(request.getBloodGroup())) {
            throw new RuntimeException("Your blood group (" + donorBloodGroup + ") does not match the required blood group (" + request.getBloodGroup() + ")");
        }
        
        // Create chat message
        Chat chat = new Chat();
        chat.setSender(donor);
        chat.setReceiver(request.getRequester());
        chat.setMessage(message);
        chat.setRelatedRequest(request);
        
        return chatRepository.save(chat);
    }
    
    @Transactional
    public BloodDonation confirmMatch(Long requestId, Long donorId) {
        User requester = getCurrentUser();
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new RuntimeException("Only the requester can confirm a match");
        }
        
        if (request.getStatus() != BloodRequest.Status.OPEN) {
            throw new RuntimeException("This request is no longer open");
        }
        
        User donor = userService.findById(donorId);
        
        // Create blood donation record
        BloodDonation donation = new BloodDonation();
        donation.setRequest(request);
        donation.setDonor(donor);
        donation.setRequester(requester);
        donation.setBloodGroup(request.getBloodGroup());
        donation.setHospitalName(request.getHospitalName());
        donation.setStatus(BloodDonation.Status.PENDING_CONFIRMATION);
        donation.setDonationDate(LocalDate.now()); // Set donation date to current date
        
        BloodDonation savedDonation = bloodDonationRepository.save(donation);
        
        // Update request status
        request.setStatus(BloodRequest.Status.MATCHED);
        request.setMatchedDonor(donor);
        request.setMatchedAt(LocalDateTime.now());
        bloodRequestRepository.save(request);
        
        return savedDonation;
    }
    
    @Transactional
    public void confirmDonation(Long donationId, boolean isDonor) {
        User currentUser = getCurrentUser();
        BloodDonation donation = bloodDonationRepository.findById(donationId)
            .orElseThrow(() -> new RuntimeException("Blood donation not found"));
        
        if (isDonor) {
            if (!donation.getDonor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Only the donor can confirm donation");
            }
            donation.setDonorConfirmed(true);
        } else {
            if (!donation.getRequester().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Only the requester can confirm receipt");
            }
            donation.setRequesterConfirmed(true);
        }
        
        // If both confirmed, enable location sharing and update status
        if (donation.getDonorConfirmed() && donation.getRequesterConfirmed()) {
            donation.setStatus(BloodDonation.Status.CONFIRMED);
            
            // Enable location sharing
            BloodRequest request = donation.getRequest();
            if (request != null) {
                request.setLocationSharingEnabled(true);
                request.setStatus(BloodRequest.Status.IN_PROGRESS);
                bloodRequestRepository.save(request);
                
                // Create location session
                LocationSession session = new LocationSession();
                session.setRequest(request);
                session.setDonor(donation.getDonor());
                session.setRequester(donation.getRequester());
                session.setIsActive(true);
                session.setStartedAt(LocalDateTime.now());
                locationSessionRepository.save(session);
            }
        }
        
        bloodDonationRepository.save(donation);
    }
    
    @Transactional
    public void completeDonation(Long donationId) {
        User requester = getCurrentUser();
        BloodDonation donation = bloodDonationRepository.findById(donationId)
            .orElseThrow(() -> new RuntimeException("Blood donation not found"));
        
        if (!donation.getRequester().getId().equals(requester.getId())) {
            throw new RuntimeException("Only the requester can complete the donation");
        }
        
        if (donation.getStatus() != BloodDonation.Status.CONFIRMED) {
            throw new RuntimeException("Donation must be confirmed before completion");
        }
        
        // Update donation status
        donation.setStatus(BloodDonation.Status.COMPLETED);
        donation.setCompletedAt(LocalDateTime.now());
        donation.setDonationDate(LocalDate.now());
        bloodDonationRepository.save(donation);
        
        // Update impact scores
        impactScoreService.addImpactScore(
            donation.getDonor().getId(),
            donation.getImpactScoreDonor(),
            "BLOOD_DONATION",
            donation.getId(),
            "Blood donation completed"
        );
        
        impactScoreService.deductImpactScore(
            donation.getRequester().getId(),
            Math.abs(donation.getImpactScoreRequester()),
            "BLOOD_REQUEST",
            donation.getId(),
            "Blood request fulfilled"
        );
        
        // Update user stats
        User donor = donation.getDonor();
        donor.setTotalBloodDonations(donor.getTotalBloodDonations() + 1);
        donor.setLastDonationDate(LocalDate.now());
        userService.save(donor);
        
        // Update request status
        BloodRequest request = donation.getRequest();
        if (request != null) {
            request.setStatus(BloodRequest.Status.COMPLETED);
            request.setCompletedAt(LocalDateTime.now());
            request.setLocationSharingEnabled(false);
            bloodRequestRepository.save(request);
            
            // End location session
            locationSessionRepository.findByRequestIdAndIsActiveTrue(request.getId())
                .ifPresent(session -> {
                    session.setIsActive(false);
                    session.setEndedAt(LocalDateTime.now());
                    locationSessionRepository.save(session);
                });
        }
    }
    
    public List<Chat> getChatMessages(Long requestId) {
        return chatRepository.findByRelatedRequestIdOrderByCreatedAtAsc(requestId);
    }
    
    public List<Chat> getChatMessagesForUser(Long requestId, Long userId, Long otherUserId) {
        if (otherUserId != null)
            return chatRepository.findMessagesBetweenUsers(requestId, userId, otherUserId);
        return chatRepository.findActiveMessagesByRequestIdAndUserId(requestId, userId);
    }
    
    public Long getUnreadCountForRequest(Long requestId, Long userId) {
        return chatRepository.countUnreadByRequestIdAndReceiverId(requestId, userId);
    }
    
    @Transactional
    public void markMessagesAsRead(Long requestId, Long userId, Long otherUserId) {
        List<Chat> messages = otherUserId != null
            ? chatRepository.findMessagesBetweenUsers(requestId, userId, otherUserId)
            : chatRepository.findByRequestIdAndUserId(requestId, userId);
        for (Chat m : messages) {
            if (m.getReceiver().getId().equals(userId) && Boolean.FALSE.equals(m.getIsRead())) {
                m.setIsRead(true);
            }
        }
        chatRepository.saveAll(messages);
    }
    
    public List<com.kindcircle.dto.ConversationResponse> getUserConversations(Long userId) {
        List<Object[]> pairs = chatRepository.findActiveConversations(userId);
        return pairs.stream()
            .map(p -> {
                Long requestId = (Long) p[0];
                Long otherUserId = (Long) p[1];
                BloodRequest request = bloodRequestRepository.findById(requestId).orElse(null);
                if (request == null) return null;
                List<Chat> last = chatRepository.findLastMessageBetweenUsers(requestId, userId, otherUserId);
                Chat lastMessage = last.isEmpty() ? null : last.get(0);
                Long unread = chatRepository.countUnreadFromUser(requestId, userId, otherUserId);
                com.kindcircle.dto.ConversationResponse r =
                    com.kindcircle.dto.ConversationResponse.fromRequestAndLastMessage(request, lastMessage, unread, userId);
                r.setOtherUser(com.kindcircle.dto.ConversationResponse.UserInfo.fromUser(userService.findById(otherUserId)));
                bloodDonationRepository.findByRequestId(requestId).ifPresent(d ->
                    r.setDonationInfo(com.kindcircle.dto.ConversationResponse.DonationInfo.fromDonation(d)));
                return r;
            })
            .filter(c -> c != null)
            .sorted((a, b) -> b.getLastActivityAt().compareTo(a.getLastActivityAt()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Transactional
    public Chat sendMessage(Long requestId, String message, Long receiverId) {
        User sender = getCurrentUser();
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        User receiver;
        if (receiverId != null) {
            receiver = userService.findById(receiverId);
        } else {
            boolean isRequester = sender.getId().equals(request.getRequester().getId());
            if (isRequester) {
                if (request.getMatchedDonor() != null)
                    receiver = request.getMatchedDonor();
                else
                    throw new RuntimeException("Recipient must be specified. Reply from a specific conversation.");
            } else {
                receiver = request.getRequester();
            }
        }
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setMessage(message);
        chat.setRelatedRequest(request);
        return chatRepository.save(chat);
    }
    
    @Transactional
    public LocationSession updateLocation(Long requestId, Double lat, Double lng, boolean isDonor) {
        User currentUser = getCurrentUser();
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        if (!request.getLocationSharingEnabled()) {
            throw new RuntimeException("Location sharing is not enabled for this request");
        }
        
        LocationSession session = locationSessionRepository.findByRequestIdAndIsActiveTrue(requestId)
            .orElseThrow(() -> new RuntimeException("Active location session not found"));
        
        if (isDonor) {
            if (!session.getDonor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized");
            }
            session.setDonorLocationLat(java.math.BigDecimal.valueOf(lat));
            session.setDonorLocationLng(java.math.BigDecimal.valueOf(lng));
        } else {
            if (!session.getRequester().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized");
            }
            session.setRequesterLocationLat(java.math.BigDecimal.valueOf(lat));
            session.setRequesterLocationLng(java.math.BigDecimal.valueOf(lng));
        }
        
        return locationSessionRepository.save(session);
    }
    
    public LocationSession getLocationSession(Long requestId) {
        return locationSessionRepository.findByRequestIdAndIsActiveTrue(requestId)
            .orElseThrow(() -> new RuntimeException("Active location session not found"));
    }
    
    public List<BloodRequest> getUserRequests(Long userId) {
        return bloodRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId);
    }
    
    public List<BloodDonation> getUserDonations(Long userId) {
        return bloodDonationRepository.findByDonorIdOrderByDonationDateDesc(userId);
    }
    
    public BloodDonation getDonationByRequestId(Long requestId) {
        return bloodDonationRepository.findByRequestId(requestId)
            .orElseThrow(() -> new RuntimeException("Donation not found for this request"));
    }
    
    public List<User> getRespondersForRequest(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        // Get all unique donors who have sent messages for this request
        List<Chat> chats = chatRepository.findByRelatedRequestIdOrderByCreatedAtAsc(requestId);
        return chats.stream()
            .filter(chat -> !chat.getSender().getId().equals(request.getRequester().getId()))
            .map(Chat::getSender)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Transactional
    public void markAsManaged(Long requestId) {
        User currentUser = getCurrentUser();
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        // Only the requester can mark their own request as managed
        if (!request.getRequester().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only mark your own blood requests as managed");
        }
        
        // Can only mark OPEN requests as managed
        if (request.getStatus() != BloodRequest.Status.OPEN) {
            throw new RuntimeException("Only open requests can be marked as managed");
        }
        
        // Update request status to MANAGED
        request.setStatus(BloodRequest.Status.MANAGED);
        request.setUpdatedAt(LocalDateTime.now());
        bloodRequestRepository.save(request);
    }
    
    @Transactional
    public void deleteRequest(Long requestId) {
        User currentUser = getCurrentUser();
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        // Only the requester can delete their own request
        if (!request.getRequester().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own blood requests");
        }
        
        // Check if request can be deleted (only OPEN or CANCELLED requests can be deleted)
        if (request.getStatus() == BloodRequest.Status.MATCHED || 
            request.getStatus() == BloodRequest.Status.IN_PROGRESS ||
            request.getStatus() == BloodRequest.Status.COMPLETED) {
            throw new RuntimeException("Cannot delete a request that is matched, in progress, or completed");
        }
        
        // Delete related data (cascade will handle most, but we'll be explicit)
        // Delete chats
        List<Chat> chats = chatRepository.findByRelatedRequestIdOrderByCreatedAtAsc(requestId);
        chatRepository.deleteAll(chats);
        
        // Delete location sessions
        locationSessionRepository.findByRequestId(requestId).ifPresent(session -> {
            locationSessionRepository.delete(session);
        });
        
        // Delete blood donations if any
        bloodDonationRepository.findByRequestId(requestId).ifPresent(donation -> {
            bloodDonationRepository.delete(donation);
        });
        
        // Delete the request
        bloodRequestRepository.delete(request);
    }
    
    @Transactional
    public void deleteChatMessage(Long messageId) {
        User currentUser = getCurrentUser();
        Chat message = chatRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Only the sender can delete their own message
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own messages");
        }
        
        message.setIsDeleted(true);
        chatRepository.save(message);
    }
    
    @Transactional
    public void archiveConversation(Long requestId, Long otherUserId) {
        User currentUser = getCurrentUser();
        List<Chat> messages = otherUserId != null
            ? chatRepository.findMessagesBetweenUsers(requestId, currentUser.getId(), otherUserId)
            : chatRepository.findNonDeletedMessagesByRequestIdAndUserId(requestId, currentUser.getId());
        for (Chat m : messages) {
            m.setIsArchived(true);
            m.setArchivedByUserId(currentUser.getId());
        }
        chatRepository.saveAll(messages);
    }
    
    @Transactional
    public void deleteConversation(Long requestId, Long otherUserId) {
        User currentUser = getCurrentUser();
        List<Chat> messages = otherUserId != null
            ? chatRepository.findMessagesBetweenUsers(requestId, currentUser.getId(), otherUserId)
            : chatRepository.findNonDeletedMessagesByRequestIdAndUserId(requestId, currentUser.getId());
        for (Chat m : messages) m.setIsDeleted(true);
        chatRepository.saveAll(messages);
    }
    
    public List<com.kindcircle.dto.ConversationResponse> getArchivedConversations(Long userId) {
        List<Object[]> pairs = chatRepository.findArchivedConversations(userId);
        return pairs.stream()
            .map(p -> {
                Long requestId = (Long) p[0];
                Long otherUserId = (Long) p[1];
                BloodRequest request = bloodRequestRepository.findById(requestId).orElse(null);
                if (request == null) return null;
                List<Chat> last = chatRepository.findLastMessageBetweenUsers(requestId, userId, otherUserId);
                Chat lastMessage = last.isEmpty() ? null : last.get(0);
                com.kindcircle.dto.ConversationResponse r =
                    com.kindcircle.dto.ConversationResponse.fromRequestAndLastMessage(request, lastMessage, 0L, userId);
                r.setOtherUser(com.kindcircle.dto.ConversationResponse.UserInfo.fromUser(userService.findById(otherUserId)));
                bloodDonationRepository.findByRequestId(requestId).ifPresent(d ->
                    r.setDonationInfo(com.kindcircle.dto.ConversationResponse.DonationInfo.fromDonation(d)));
                return r;
            })
            .filter(c -> c != null)
            .sorted((a, b) -> b.getLastActivityAt().compareTo(a.getLastActivityAt()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}

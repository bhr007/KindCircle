package com.kindcircle.service;

import com.kindcircle.model.Notification;
import com.kindcircle.model.User;
import com.kindcircle.repository.NotificationRepository;
import com.kindcircle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Notification createNotification(Long userId, Notification.NotificationType type, String message, Long relatedPostId, Long relatedUserId) {
        return createNotification(userId, type, message, relatedPostId, null, relatedUserId);
    }

    @Transactional
    public Notification createNotification(Long userId, Notification.NotificationType type, String message,
            Long relatedPostId, Long relatedCampaignId, Long relatedUserId) {
        User user = userService.findById(userId);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setNotificationType(type);
        notification.setMessage(message);
        notification.setRelatedPostId(relatedPostId);
        notification.setRelatedCampaignId(relatedCampaignId);
        notification.setRelatedUserId(relatedUserId);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Transactional
    public void notifyAdminsForPostPending(Long postId, Long authorUserId, String authorName, String title) {
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        String msg = String.format("New post pending approval: \"%s\" by %s",
                (title != null && !title.isBlank()) ? title : "Untitled",
                (authorName != null && !authorName.isBlank()) ? authorName : "a user");
        for (User admin : admins) {
            createNotification(admin.getId(), Notification.NotificationType.POST_PENDING_APPROVAL, msg, postId, null, authorUserId);
        }
    }

    @Transactional
    public void notifyAdminsForCampaignPending(Long campaignId, Long organizerId, String organizerName, String campaignTitle) {
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        String msg = String.format("New campaign pending approval: \"%s\" by %s",
                (campaignTitle != null && !campaignTitle.isBlank()) ? campaignTitle : "Untitled",
                (organizerName != null && !organizerName.isBlank()) ? organizerName : "a user");
        for (User admin : admins) {
            createNotification(admin.getId(), Notification.NotificationType.CAMPAIGN_PENDING_APPROVAL, msg, null, campaignId, organizerId);
        }
    }
    
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Ensure user owns this notification
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}

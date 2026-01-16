package com.kindcircle.repository;

import com.kindcircle.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findBySenderIdAndReceiverIdOrderByCreatedAtAsc(Long senderId, Long receiverId);
    List<Chat> findByRelatedRequestIdOrderByCreatedAtAsc(Long requestId);
    List<Chat> findByReceiverIdAndIsReadFalse(Long receiverId);
    Long countByReceiverIdAndIsReadFalse(Long receiverId);
    
    // Get all chats where user is sender or receiver for a specific request
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId AND (c.sender.id = :userId OR c.receiver.id = :userId) ORDER BY c.createdAt ASC")
    List<Chat> findByRequestIdAndUserId(@Param("requestId") Long requestId, @Param("userId") Long userId);
    
    // Get unread count for a specific request and user
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.relatedRequest.id = :requestId AND c.receiver.id = :userId AND c.isRead = false")
    Long countUnreadByRequestIdAndReceiverId(@Param("requestId") Long requestId, @Param("userId") Long userId);
    
    // Get last message for a request
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId ORDER BY c.createdAt DESC")
    List<Chat> findLastMessageByRequestId(@Param("requestId") Long requestId);
    
    // Get all distinct requests where user has participated
    @Query("SELECT DISTINCT c.relatedRequest.id FROM Chat c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.relatedRequest IS NOT NULL")
    List<Long> findDistinctRequestIdsByUserId(@Param("userId") Long userId);
}

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
    
    // Get last message for a request (non-deleted)
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt DESC")
    List<Chat> findLastMessageByRequestId(@Param("requestId") Long requestId);
    
    // Get last message for a request and user (non-deleted for that user)
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId AND (c.sender.id = :userId OR c.receiver.id = :userId) AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt DESC")
    List<Chat> findLastMessageByRequestIdAndUserId(@Param("requestId") Long requestId, @Param("userId") Long userId);
    
    // Get all distinct requests where user has participated
    @Query("SELECT DISTINCT c.relatedRequest.id FROM Chat c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.relatedRequest IS NOT NULL")
    List<Long> findDistinctRequestIdsByUserId(@Param("userId") Long userId);
    
    // Find messages that are not deleted for a user
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId AND (c.sender.id = :userId OR c.receiver.id = :userId) AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt ASC")
    List<Chat> findActiveMessagesByRequestIdAndUserId(@Param("requestId") Long requestId, @Param("userId") Long userId);
    
    // Find conversations that are not archived and not deleted for a user
    // Returns Object[] { requestId, otherUserId } — one row per distinct (request, other-user) thread
    @Query("SELECT DISTINCT c.relatedRequest.id, " +
           "CASE WHEN c.sender.id = :userId THEN c.receiver.id ELSE c.sender.id END " +
           "FROM Chat c " +
           "WHERE (c.sender.id = :userId OR c.receiver.id = :userId) " +
           "AND c.relatedRequest IS NOT NULL " +
           "AND (c.isDeleted = false OR c.isDeleted IS NULL) " +
           "AND (c.isArchived = false OR c.isArchived IS NULL OR c.archivedByUserId != :userId OR c.archivedByUserId IS NULL)")
    List<Object[]> findActiveConversations(@Param("userId") Long userId);

    // Archived conversations: same (requestId, otherUserId) shape
    @Query("SELECT DISTINCT c.relatedRequest.id, " +
           "CASE WHEN c.sender.id = :userId THEN c.receiver.id ELSE c.sender.id END " +
           "FROM Chat c " +
           "WHERE (c.sender.id = :userId OR c.receiver.id = :userId) " +
           "AND c.relatedRequest IS NOT NULL " +
           "AND c.isArchived = true AND c.archivedByUserId = :userId " +
           "AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    List<Object[]> findArchivedConversations(@Param("userId") Long userId);

    // Messages between two users for a specific request (1-to-1 thread)
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId " +
           "AND ((c.sender.id = :userId AND c.receiver.id = :otherUserId) OR (c.sender.id = :otherUserId AND c.receiver.id = :userId)) " +
           "AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt ASC")
    List<Chat> findMessagesBetweenUsers(@Param("requestId") Long requestId,
                                        @Param("userId") Long userId,
                                        @Param("otherUserId") Long otherUserId);

    // Last message in a (requestId, userId, otherUserId) thread
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId " +
           "AND ((c.sender.id = :userId AND c.receiver.id = :otherUserId) OR (c.sender.id = :otherUserId AND c.receiver.id = :userId)) " +
           "AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt DESC")
    List<Chat> findLastMessageBetweenUsers(@Param("requestId") Long requestId,
                                           @Param("userId") Long userId,
                                           @Param("otherUserId") Long otherUserId);

    // Unread count from a specific user in a request thread
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.relatedRequest.id = :requestId " +
           "AND c.receiver.id = :userId AND c.sender.id = :otherUserId AND c.isRead = false")
    Long countUnreadFromUser(@Param("requestId") Long requestId,
                             @Param("userId") Long userId,
                             @Param("otherUserId") Long otherUserId);

    // Find conversations that are not deleted for a user (for deleteConversation check)
    @Query("SELECT c FROM Chat c WHERE c.relatedRequest.id = :requestId AND (c.sender.id = :userId OR c.receiver.id = :userId) AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt ASC")
    List<Chat> findNonDeletedMessagesByRequestIdAndUserId(@Param("requestId") Long requestId, @Param("userId") Long userId);
}

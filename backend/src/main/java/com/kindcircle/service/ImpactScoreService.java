package com.kindcircle.service;

import com.kindcircle.model.ImpactScoreLedger;
import com.kindcircle.model.User;
import com.kindcircle.repository.ImpactScoreLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ImpactScoreService {
    
    @Autowired
    private ImpactScoreLedgerRepository ledgerRepository;
    
    @Autowired
    private UserService userService;
    
    @Transactional
    public void addImpactScore(Long userId, int points, String activityType, Long referenceId, String description) {
        addImpactScore(userId, points, activityType, referenceId, description, "SOCIAL_WORK_POST");
    }

    @Transactional
    public void addImpactScore(Long userId, int points, String activityType, Long referenceId, String description, String referenceType) {
        User user = userService.findById(userId);
        user.setCurrentImpactScore(user.getCurrentImpactScore() + points);
        userService.save(user);
        
        ImpactScoreLedger ledger = new ImpactScoreLedger();
        ledger.setUserId(userId);
        ledger.setTransactionType(ImpactScoreLedger.TransactionType.EARNED);
        ledger.setActivityType(ImpactScoreLedger.ActivityType.valueOf(activityType));
        ledger.setPoints(points);
        ledger.setDescription(description);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(referenceType != null ? referenceType : "SOCIAL_WORK_POST");
        ledger.setCreatedAt(LocalDateTime.now());
        
        ledgerRepository.save(ledger);
    }
    
    @Transactional
    public void deductImpactScore(Long userId, int points, String activityType, Long referenceId, String description) {
        User user = userService.findById(userId);
        if (user.getCurrentImpactScore() < points) {
            throw new RuntimeException("Insufficient Impact Score");
        }
        user.setCurrentImpactScore(user.getCurrentImpactScore() - points);
        userService.save(user);
        
        ImpactScoreLedger ledger = new ImpactScoreLedger();
        ledger.setUserId(userId);
        ledger.setTransactionType(ImpactScoreLedger.TransactionType.SPENT);
        ledger.setActivityType(ImpactScoreLedger.ActivityType.valueOf(activityType));
        ledger.setPoints(-points);
        ledger.setDescription(description);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType("BLOOD_REQUEST");
        ledger.setCreatedAt(LocalDateTime.now());
        
        ledgerRepository.save(ledger);
    }
}

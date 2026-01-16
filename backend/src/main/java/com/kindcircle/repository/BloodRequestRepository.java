package com.kindcircle.repository;

import com.kindcircle.model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByStatusOrderByCreatedAtDesc(BloodRequest.Status status);
    List<BloodRequest> findByBloodGroupAndStatus(String bloodGroup, BloodRequest.Status status);
    List<BloodRequest> findByStatusAndBloodGroupOrderByCreatedAtDesc(BloodRequest.Status status, String bloodGroup);
    List<BloodRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<BloodRequest> findByMatchedDonorIdOrderByCreatedAtDesc(Long donorId);
    List<BloodRequest> findByCityAndStatusOrderByCreatedAtDesc(String city, BloodRequest.Status status);
}

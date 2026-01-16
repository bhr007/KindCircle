package com.kindcircle.repository;

import com.kindcircle.model.BloodDonation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodDonationRepository extends JpaRepository<BloodDonation, Long> {
    List<BloodDonation> findByDonorIdOrderByDonationDateDesc(Long donorId);
    List<BloodDonation> findByRequesterIdOrderByDonationDateDesc(Long requesterId);
    Optional<BloodDonation> findByRequestId(Long requestId);
    List<BloodDonation> findByStatus(BloodDonation.Status status);
}

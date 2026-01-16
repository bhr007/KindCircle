package com.kindcircle.repository;

import com.kindcircle.model.LocationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationSessionRepository extends JpaRepository<LocationSession, Long> {
    Optional<LocationSession> findByRequestIdAndIsActiveTrue(Long requestId);
    Optional<LocationSession> findByRequestId(Long requestId);
}

package com.kindcircle.repository;

import com.kindcircle.model.ImpactScoreLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImpactScoreLedgerRepository extends JpaRepository<ImpactScoreLedger, Long> {
    List<ImpactScoreLedger> findByUserIdOrderByCreatedAtDesc(Long userId);
}

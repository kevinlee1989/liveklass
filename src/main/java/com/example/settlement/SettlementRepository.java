package com.example.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByCreatorIdAndMonth(String creatorId, String month);
}

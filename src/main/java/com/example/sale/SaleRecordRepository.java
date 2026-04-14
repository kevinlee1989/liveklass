package com.example.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SaleRecordRepository extends JpaRepository<SaleRecord, String> {

    @Query("SELECT s FROM SaleRecord s WHERE s.course.creator.id = :creatorId AND s.paidAt >= :from AND s.paidAt < :to")
    List<SaleRecord> findByCreatorAndPaidAtBetween(
            @Param("creatorId") String creatorId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("SELECT s FROM SaleRecord s WHERE s.paidAt >= :from AND s.paidAt < :to")
    List<SaleRecord> findByPaidAtBetween(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("SELECT s FROM SaleRecord s WHERE s.course.creator.id = :creatorId")
    List<SaleRecord> findByCreatorId(@Param("creatorId") String creatorId);
}

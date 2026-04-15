package com.example.cancellation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface CancellationRecordRepository extends JpaRepository<CancellationRecord, Long> {

    @Query("SELECT c FROM CancellationRecord c WHERE c.saleRecord.course.creator.id = :creatorId AND c.canceledAt >= :from AND c.canceledAt < :to")
    List<CancellationRecord> findByCreatorAndCanceledAtBetween(
            @Param("creatorId") String creatorId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("SELECT c FROM CancellationRecord c WHERE c.canceledAt >= :from AND c.canceledAt < :to")
    List<CancellationRecord> findByCanceledAtBetween(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("SELECT COALESCE(SUM(c.refundAmount), 0) FROM CancellationRecord c WHERE c.saleRecord.id = :saleRecordId")
    java.math.BigDecimal sumRefundAmountBySaleRecordId(@Param("saleRecordId") String saleRecordId);
}

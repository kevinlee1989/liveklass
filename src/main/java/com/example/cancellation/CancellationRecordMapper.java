package com.example.cancellation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface CancellationRecordMapper {

    void insert(CancellationRecord cancellationRecord);

    BigDecimal sumRefundAmountBySaleRecordId(@Param("saleRecordId") String saleRecordId);

    List<CancellationRecord> findByCanceledAtBetween(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    List<CancellationRecord> findByCreatorAndCanceledAtBetween(
            @Param("creatorId") String creatorId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}

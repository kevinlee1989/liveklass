package com.example.sale;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SaleRecordMapper {
    boolean existsById(String id);
    void insert(SaleRecord saleRecord);
    Optional<SaleRecord> findById(String id);
    List<SaleRecord> findByCreatorId(String creatorId);
    List<SaleRecord> findByCreatorAndPaidAtBetween(@Param("creatorId") String creatorId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    List<SaleRecord> findByPaidAtBetween(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}

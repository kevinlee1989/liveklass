package com.example.sale.dto;

import com.example.sale.SaleRecord;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaleRecordResponse(
        String id,
        String courseId,
        String courseTitle,
        String creatorId,
        String studentId,
        BigDecimal amount,
        OffsetDateTime paidAt
) {
    public static SaleRecordResponse from(SaleRecord saleRecord) {
        return new SaleRecordResponse(
                saleRecord.getId(),
                saleRecord.getCourse().getId(),
                saleRecord.getCourse().getTitle(),
                saleRecord.getCourse().getCreator().getId(),
                saleRecord.getStudentId(),
                saleRecord.getAmount(),
                saleRecord.getPaidAt()
        );
    }
}

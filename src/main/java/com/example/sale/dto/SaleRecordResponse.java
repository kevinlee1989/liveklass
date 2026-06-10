package com.example.sale.dto;

import com.example.course.Course;
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
    public static SaleRecordResponse from(SaleRecord saleRecord, Course course) {
        return new SaleRecordResponse(
                saleRecord.getId(),
                course.getId(),
                course.getTitle(),
                course.getCreatorId(),
                saleRecord.getStudentId(),
                saleRecord.getAmount(),
                saleRecord.getPaidAt()
        );
    }
}

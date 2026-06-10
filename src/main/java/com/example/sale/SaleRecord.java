package com.example.sale;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SaleRecord {

    private String id;
    private String courseId;
    private String studentId;
    private BigDecimal amount;
    private OffsetDateTime paidAt;

    public static SaleRecord of(String id, String courseId, String studentId, BigDecimal amount, OffsetDateTime paidAt) {
        SaleRecord record = new SaleRecord();
        record.id = id;
        record.courseId = courseId;
        record.studentId = studentId;
        record.amount = amount;
        record.paidAt = paidAt;
        return record;
    }
}

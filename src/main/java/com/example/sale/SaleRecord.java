package com.example.sale;

import com.example.course.Course;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sale_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleRecord {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_at", nullable = false)
    private OffsetDateTime paidAt;

    public static SaleRecord of(String id, Course course, String studentId, BigDecimal amount, OffsetDateTime paidAt) {
        SaleRecord record = new SaleRecord();
        record.id = id;
        record.course = course;
        record.studentId = studentId;
        record.amount = amount;
        record.paidAt = paidAt;
        return record;
    }
}

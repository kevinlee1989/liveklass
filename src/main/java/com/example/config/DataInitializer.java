package com.example.config;

import com.example.cancellation.CancellationRecord;
import com.example.cancellation.CancellationRecordMapper;
import com.example.course.Course;
import com.example.course.CourseMapper;
import com.example.creator.Creator;
import com.example.creator.CreatorMapper;
import com.example.sale.SaleRecord;
import com.example.sale.SaleRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CreatorMapper creatorMapper;
    private final CourseMapper courseMapper;
    private final SaleRecordMapper saleRecordMapper;
    private final CancellationRecordMapper cancellationRecordMapper;

    @Override
    public void run(ApplicationArguments args) {
        if (creatorMapper.existsById("creator-1")) return;

        // Creators
        Creator creator1 = Creator.of("creator-1", "김강사");
        Creator creator2 = Creator.of("creator-2", "이강사");
        Creator creator3 = Creator.of("creator-3", "박강사");
        creatorMapper.insert(creator1);
        creatorMapper.insert(creator2);
        creatorMapper.insert(creator3);

        // Courses
        Course course1 = Course.of("course-1", creator1.getId(), "Spring Boot 입문");
        Course course2 = Course.of("course-2", creator1.getId(), "JPA 실전");
        Course course3 = Course.of("course-3", creator2.getId(), "Kotlin 기초");
        Course course4 = Course.of("course-4", creator3.getId(), "MSA 설계");
        courseMapper.insert(course1);
        courseMapper.insert(course2);
        courseMapper.insert(course3);
        courseMapper.insert(course4);

        // SaleRecords
        SaleRecord sale1 = SaleRecord.of("sale-1", course1.getId(), "student-1",
                new BigDecimal("50000"), OffsetDateTime.parse("2025-03-05T10:00:00+09:00"));
        SaleRecord sale2 = SaleRecord.of("sale-2", course1.getId(), "student-2",
                new BigDecimal("50000"), OffsetDateTime.parse("2025-03-15T14:30:00+09:00"));
        SaleRecord sale3 = SaleRecord.of("sale-3", course2.getId(), "student-3",
                new BigDecimal("80000"), OffsetDateTime.parse("2025-03-20T09:00:00+09:00"));
        SaleRecord sale4 = SaleRecord.of("sale-4", course2.getId(), "student-4",
                new BigDecimal("80000"), OffsetDateTime.parse("2025-03-22T11:00:00+09:00"));
        SaleRecord sale5 = SaleRecord.of("sale-5", course3.getId(), "student-5",
                new BigDecimal("60000"), OffsetDateTime.parse("2025-01-31T23:30:00+09:00"));
        SaleRecord sale6 = SaleRecord.of("sale-6", course3.getId(), "student-6",
                new BigDecimal("60000"), OffsetDateTime.parse("2025-03-10T16:00:00+09:00"));
        SaleRecord sale7 = SaleRecord.of("sale-7", course4.getId(), "student-7",
                new BigDecimal("120000"), OffsetDateTime.parse("2025-02-14T10:00:00+09:00"));

        saleRecordMapper.insert(sale1);
        saleRecordMapper.insert(sale2);
        saleRecordMapper.insert(sale3);
        saleRecordMapper.insert(sale4);
        saleRecordMapper.insert(sale5);
        saleRecordMapper.insert(sale6);
        saleRecordMapper.insert(sale7);

        // CancellationRecords
        CancellationRecord cancel1 = CancellationRecord.of(sale3.getId(),
                new BigDecimal("80000"), OffsetDateTime.parse("2025-03-25T10:00:00+09:00"));
        CancellationRecord cancel2 = CancellationRecord.of(sale4.getId(),
                new BigDecimal("30000"), OffsetDateTime.parse("2025-03-28T10:00:00+09:00"));
        CancellationRecord cancel3 = CancellationRecord.of(sale5.getId(),
                new BigDecimal("60000"), OffsetDateTime.parse("2025-02-03T09:00:00+09:00"));

        cancellationRecordMapper.insert(cancel1);
        cancellationRecordMapper.insert(cancel2);
        cancellationRecordMapper.insert(cancel3);
    }
}

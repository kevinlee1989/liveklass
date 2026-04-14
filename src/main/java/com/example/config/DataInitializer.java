package com.example.config;

import com.example.cancellation.CancellationRecord;
import com.example.cancellation.CancellationRecordRepository;
import com.example.course.Course;
import com.example.course.CourseRepository;
import com.example.creator.Creator;
import com.example.creator.CreatorRepository;
import com.example.sale.SaleRecord;
import com.example.sale.SaleRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CreatorRepository creatorRepository;
    private final CourseRepository courseRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final CancellationRecordRepository cancellationRecordRepository;

    @Override
    public void run(ApplicationArguments args) {
        // Creators
        Creator creator1 = Creator.of("creator-1", "김강사");
        Creator creator2 = Creator.of("creator-2", "이강사");
        Creator creator3 = Creator.of("creator-3", "박강사");
        creatorRepository.save(creator1);
        creatorRepository.save(creator2);
        creatorRepository.save(creator3);

        // Courses
        Course course1 = Course.of("course-1", creator1, "Spring Boot 입문");
        Course course2 = Course.of("course-2", creator1, "JPA 실전");
        Course course3 = Course.of("course-3", creator2, "Kotlin 기초");
        Course course4 = Course.of("course-4", creator3, "MSA 설계");
        courseRepository.save(course1);
        courseRepository.save(course2);
        courseRepository.save(course3);
        courseRepository.save(course4);

        // SaleRecords
        SaleRecord sale1 = SaleRecord.of("sale-1", course1, "student-1",
                new BigDecimal("50000"), OffsetDateTime.parse("2025-03-05T10:00:00+09:00"));
        SaleRecord sale2 = SaleRecord.of("sale-2", course1, "student-2",
                new BigDecimal("50000"), OffsetDateTime.parse("2025-03-15T14:30:00+09:00"));
        SaleRecord sale3 = SaleRecord.of("sale-3", course2, "student-3",
                new BigDecimal("80000"), OffsetDateTime.parse("2025-03-20T09:00:00+09:00"));
        SaleRecord sale4 = SaleRecord.of("sale-4", course2, "student-4",
                new BigDecimal("80000"), OffsetDateTime.parse("2025-03-22T11:00:00+09:00"));
        SaleRecord sale5 = SaleRecord.of("sale-5", course3, "student-5",
                new BigDecimal("60000"), OffsetDateTime.parse("2025-01-31T23:30:00+09:00"));
        SaleRecord sale6 = SaleRecord.of("sale-6", course3, "student-6",
                new BigDecimal("60000"), OffsetDateTime.parse("2025-03-10T16:00:00+09:00"));
        SaleRecord sale7 = SaleRecord.of("sale-7", course4, "student-7",
                new BigDecimal("120000"), OffsetDateTime.parse("2025-02-14T10:00:00+09:00"));

        saleRecordRepository.save(sale1);
        saleRecordRepository.save(sale2);
        saleRecordRepository.save(sale3);
        saleRecordRepository.save(sale4);
        saleRecordRepository.save(sale5);
        saleRecordRepository.save(sale6);
        saleRecordRepository.save(sale7);

        // CancellationRecords
        CancellationRecord cancel1 = CancellationRecord.of(sale3,
                new BigDecimal("80000"), OffsetDateTime.parse("2025-03-25T10:00:00+09:00"));
        CancellationRecord cancel2 = CancellationRecord.of(sale4,
                new BigDecimal("30000"), OffsetDateTime.parse("2025-03-28T10:00:00+09:00"));
        CancellationRecord cancel3 = CancellationRecord.of(sale5,
                new BigDecimal("60000"), OffsetDateTime.parse("2025-02-03T09:00:00+09:00"));

        cancellationRecordRepository.save(cancel1);
        cancellationRecordRepository.save(cancel2);
        cancellationRecordRepository.save(cancel3);
    }
}

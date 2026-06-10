package com.example.sale;

import com.example.course.Course;
import com.example.course.CourseMapper;
import com.example.sale.dto.SaleRecordRequest;
import com.example.sale.dto.SaleRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleRecordService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SaleRecordMapper saleRecordMapper;
    private final CourseMapper courseMapper;

    @Transactional
    public String register(SaleRecordRequest request) {
        if (saleRecordMapper.existsById(request.id())) {
            throw new IllegalArgumentException("이미 존재하는 판매 내역 ID입니다: " + request.id());
        }

        if (!courseMapper.existsById(request.courseId())) {
            throw new IllegalArgumentException("존재하지 않는 강의입니다: " + request.courseId());
        }

        SaleRecord saleRecord = SaleRecord.of(
                request.id(),
                request.courseId(),
                request.studentId(),
                request.amount(),
                request.paidAt()
        );

        saleRecordMapper.insert(saleRecord);
        return saleRecord.getId();
    }

    @Transactional(readOnly = true)
    public List<SaleRecordResponse> getList(String creatorId, LocalDate from, LocalDate to) {
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("from과 to는 함께 입력하거나 함께 생략해야 합니다.");
        }

        if (from != null && from.isAfter(to)) {
            throw new IllegalArgumentException("시작일(from)은 종료일(to)보다 늦을 수 없습니다.");
        }

        List<SaleRecord> records;

        if (from != null) {
            OffsetDateTime fromDt = from.atStartOfDay(KST).toOffsetDateTime();
            OffsetDateTime toDt = to.plusDays(1).atStartOfDay(KST).toOffsetDateTime();
            records = saleRecordMapper.findByCreatorAndPaidAtBetween(creatorId, fromDt, toDt);
        } else {
            records = saleRecordMapper.findByCreatorId(creatorId);
        }

        return records.stream().map(record -> {
            Course course = courseMapper.findById(record.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다: " + record.getCourseId()));
            return SaleRecordResponse.from(record, course);
        }).toList();
    }
}

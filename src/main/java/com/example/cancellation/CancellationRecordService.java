package com.example.cancellation;

import com.example.cancellation.dto.CancellationRecordRequest;
import com.example.sale.SaleRecord;
import com.example.sale.SaleRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancellationRecordService {

    private final CancellationRecordRepository cancellationRecordRepository;
    private final SaleRecordRepository saleRecordRepository;

    @Transactional
    public Long register(CancellationRecordRequest request) {
        SaleRecord saleRecord = saleRecordRepository.findById(request.saleRecordId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매 내역입니다: " + request.saleRecordId()));

        CancellationRecord cancellationRecord = CancellationRecord.of(
                saleRecord,
                request.refundAmount(),
                request.canceledAt()
        );

        return cancellationRecordRepository.save(cancellationRecord).getId();
    }
}

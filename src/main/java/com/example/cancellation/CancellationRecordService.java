package com.example.cancellation;

import com.example.cancellation.dto.CancellationRecordRequest;
import com.example.sale.SaleRecord;
import com.example.sale.SaleRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CancellationRecordService {

    private final CancellationRecordRepository cancellationRecordRepository;
    private final SaleRecordRepository saleRecordRepository;

    @Transactional
    public Long register(CancellationRecordRequest request) {
        SaleRecord saleRecord = saleRecordRepository.findById(request.saleRecordId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매 내역입니다: " + request.saleRecordId()));

        BigDecimal existingRefunds = cancellationRecordRepository.sumRefundAmountBySaleRecordId(saleRecord.getId());
        BigDecimal totalRefund = existingRefunds.add(request.refundAmount());

        if (totalRefund.compareTo(saleRecord.getAmount()) > 0) {
            throw new IllegalArgumentException(
                    "누적 환불 금액이 원결제 금액을 초과합니다. " +
                    "원결제: " + saleRecord.getAmount() +
                    ", 기존 환불 합계: " + existingRefunds +
                    ", 요청 환불: " + request.refundAmount()
            );
        }

        CancellationRecord cancellationRecord = CancellationRecord.of(
                saleRecord,
                request.refundAmount(),
                request.canceledAt()
        );

        return cancellationRecordRepository.save(cancellationRecord).getId();
    }
}

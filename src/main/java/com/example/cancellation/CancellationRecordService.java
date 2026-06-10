package com.example.cancellation;

import com.example.cancellation.dto.CancellationRecordRequest;
import com.example.sale.SaleRecord;
import com.example.sale.SaleRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CancellationRecordService {

    private final CancellationRecordMapper cancellationRecordMapper;
    private final SaleRecordMapper saleRecordMapper;

    @Transactional
    public Long register(CancellationRecordRequest request) {
        SaleRecord saleRecord = saleRecordMapper.findById(request.saleRecordId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 내역입니다: " + request.saleRecordId()
                ));

        BigDecimal existingRefunds =
                cancellationRecordMapper.sumRefundAmountBySaleRecordId(saleRecord.getId());

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
                saleRecord.getId(),
                request.refundAmount(),
                request.canceledAt()
        );

        cancellationRecordMapper.insert(cancellationRecord);

        return cancellationRecord.getId();
    }
}
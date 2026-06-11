import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getCreatorSettlement, updateSettlementStatus, getSettlementSummary } from '../api/settlements'

export default function SettlementPage() {
  const queryClient = useQueryClient()
  const today = new Date()
  const defaultMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`

  const [creatorId, setCreatorId] = useState('')
  const [month, setMonth] = useState(defaultMonth)
  const [summaryFrom, setSummaryFrom] = useState('')
  const [summaryTo, setSummaryTo] = useState('')
  const [searchKey, setSearchKey] = useState<{ creatorId: string; month: string } | null>(null)
  const [summaryKey, setSummaryKey] = useState<{ from: string; to: string } | null>(null)

  const { data: settlement } = useQuery({
    queryKey: ['settlement', searchKey],
    queryFn: () => getCreatorSettlement(searchKey!.creatorId, searchKey!.month),
    enabled: !!searchKey,
  })

  const { data: summary } = useQuery({
    queryKey: ['settlement-summary', summaryKey],
    queryFn: () => getSettlementSummary(summaryKey!.from, summaryKey!.to),
    enabled: !!summaryKey,
  })

  const statusMutation = useMutation({
    mutationFn: ({ status }: { status: 'CONFIRMED' | 'PAID' }) =>
      updateSettlementStatus(searchKey!.creatorId, searchKey!.month, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['settlement', searchKey] }),
  })

  return (
    <div>
      <h1>정산 관리</h1>

      <section style={{ marginBottom: '2rem' }}>
        <h2>크리에이터별 정산 조회</h2>
        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
          <input
            placeholder="크리에이터 ID"
            value={creatorId}
            onChange={(e) => setCreatorId(e.target.value)}
            style={{ padding: '0.4rem' }}
          />
          <input
            type="month"
            value={month}
            onChange={(e) => setMonth(e.target.value)}
            style={{ padding: '0.4rem' }}
          />
          <button onClick={() => setSearchKey({ creatorId, month })}>조회</button>
        </div>

        {settlement && (
          <div style={{ border: '1px solid #ddd', padding: '1rem', borderRadius: '4px' }}>
            <p>상태: <strong>{settlement.status}</strong></p>
            <p>총 판매액: {settlement.totalSaleAmount.toLocaleString()}원</p>
            <p>총 환불액: {settlement.totalRefundAmount.toLocaleString()}원</p>
            <p>순 판매액: {settlement.netAmount.toLocaleString()}원</p>
            <p>수수료율: {(settlement.commissionRate * 100).toFixed(0)}%</p>
            <p>수수료: {settlement.commissionAmount.toLocaleString()}원</p>
            <p>정산액: <strong>{settlement.settlementAmount.toLocaleString()}원</strong></p>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
              {settlement.status === 'PENDING' && (
                <button onClick={() => statusMutation.mutate({ status: 'CONFIRMED' })}>
                  확정
                </button>
              )}
              {settlement.status === 'CONFIRMED' && (
                <button onClick={() => statusMutation.mutate({ status: 'PAID' })}>
                  지급 완료
                </button>
              )}
            </div>
          </div>
        )}
      </section>

      <section>
        <h2>기간별 정산 요약</h2>
        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
          <input
            type="date"
            value={summaryFrom}
            onChange={(e) => setSummaryFrom(e.target.value)}
            style={{ padding: '0.4rem' }}
          />
          <span>~</span>
          <input
            type="date"
            value={summaryTo}
            onChange={(e) => setSummaryTo(e.target.value)}
            style={{ padding: '0.4rem' }}
          />
          <button onClick={() => setSummaryKey({ from: summaryFrom, to: summaryTo })}>
            조회
          </button>
        </div>

        {summary && (
          <div style={{ border: '1px solid #ddd', padding: '1rem', borderRadius: '4px' }}>
            <p>총 정산액: {summary.totalSettlementAmount.toLocaleString()}원</p>
            <p>지급 완료: {summary.paidAmount.toLocaleString()}원</p>
            <p>미지급: {summary.pendingAmount.toLocaleString()}원</p>
          </div>
        )}
      </section>
    </div>
  )
}

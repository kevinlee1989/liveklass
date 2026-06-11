import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getSaleRecords } from '../api/saleRecords'

export default function SaleRecordsPage() {
  const [creatorId, setCreatorId] = useState('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [searchParams, setSearchParams] = useState<{
    creatorId: string
    from?: string
    to?: string
  } | null>(null)

  const { data: records = [], isLoading } = useQuery({
    queryKey: ['sale-records', searchParams],
    queryFn: () => getSaleRecords(searchParams!.creatorId, searchParams?.from, searchParams?.to),
    enabled: !!searchParams,
  })

  return (
    <div>
      <h1>판매 내역</h1>

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <input
          placeholder="크리에이터 ID"
          value={creatorId}
          onChange={(e) => setCreatorId(e.target.value)}
          style={{ padding: '0.4rem' }}
        />
        <input
          type="date"
          value={from}
          onChange={(e) => setFrom(e.target.value)}
          style={{ padding: '0.4rem' }}
        />
        <span style={{ alignSelf: 'center' }}>~</span>
        <input
          type="date"
          value={to}
          onChange={(e) => setTo(e.target.value)}
          style={{ padding: '0.4rem' }}
        />
        <button
          onClick={() => setSearchParams({ creatorId, from: from || undefined, to: to || undefined })}
          disabled={!creatorId}
        >
          조회
        </button>
      </div>

      {isLoading && <p>불러오는 중...</p>}

      {searchParams && !isLoading && (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#f5f5f5' }}>
              <th style={th}>판매일</th>
              <th style={th}>강좌 ID</th>
              <th style={th}>판매액</th>
            </tr>
          </thead>
          <tbody>
            {records.length === 0 ? (
              <tr>
                <td colSpan={3} style={{ ...td, textAlign: 'center' }}>
                  데이터 없음
                </td>
              </tr>
            ) : (
              records.map((r) => (
                <tr key={r.id}>
                  <td style={td}>{r.paidAt}</td>
                  <td style={td}>{r.courseId}</td>
                  <td style={td}>{r.amount.toLocaleString()}원</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
    </div>
  )
}

const th: React.CSSProperties = { padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }
const td: React.CSSProperties = { padding: '0.5rem', border: '1px solid #ddd' }

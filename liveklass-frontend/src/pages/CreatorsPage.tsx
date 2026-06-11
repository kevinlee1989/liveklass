import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getCreators, createCreator, deleteCreator } from '../api/creators'

export default function CreatorsPage() {
  const queryClient = useQueryClient()
  const [id, setId] = useState('')
  const [name, setName] = useState('')

  const { data: creators = [], isLoading, error: listError } = useQuery({
    queryKey: ['creators'],
    queryFn: getCreators,
  })

  const createMutation = useMutation({
    mutationFn: createCreator,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['creators'] })
      setId('')
      setName('')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: deleteCreator,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['creators'] }),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (id && name) createMutation.mutate({ id, name })
  }

  return (
    <div>
      <h1>크리에이터 관리</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: '1.5rem', display: 'flex', gap: '0.5rem' }}>
        <input
          placeholder="ID"
          value={id}
          onChange={(e) => setId(e.target.value)}
          required
        />
        <input
          placeholder="이름"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <button type="submit" disabled={createMutation.isPending}>
          {createMutation.isPending ? '등록 중...' : '등록'}
        </button>
      </form>

      {createMutation.isError && (
        <p style={{ color: 'red', marginBottom: '1rem' }}>
          등록 실패: {(createMutation.error as Error).message}
        </p>
      )}

      {listError && (
        <p style={{ color: 'red' }}>목록 조회 실패: {(listError as Error).message}</p>
      )}

      {isLoading ? (
        <p>불러오는 중...</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#f5f5f5' }}>
              <th style={th}>ID</th>
              <th style={th}>이름</th>
              <th style={th}>작업</th>
            </tr>
          </thead>
          <tbody>
            {creators.length === 0 ? (
              <tr>
                <td colSpan={3} style={{ ...td, textAlign: 'center' }}>등록된 크리에이터 없음</td>
              </tr>
            ) : (
              creators.map((c) => (
                <tr key={c.id}>
                  <td style={td}>{c.id}</td>
                  <td style={td}>{c.name}</td>
                  <td style={td}>
                    <button
                      onClick={() => deleteMutation.mutate(c.id)}
                      disabled={deleteMutation.isPending}
                      style={{ color: 'red' }}
                    >
                      삭제
                    </button>
                  </td>
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

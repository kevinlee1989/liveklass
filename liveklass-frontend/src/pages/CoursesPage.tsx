import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getCourses, createCourse, updateCourseTitle, deleteCourse } from '../api/courses'

export default function CoursesPage() {
  const queryClient = useQueryClient()
  const [id, setId] = useState('')
  const [creatorId, setCreatorId] = useState('')
  const [title, setTitle] = useState('')
  const [filterCreatorId, setFilterCreatorId] = useState('')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editingTitle, setEditingTitle] = useState('')

  const { data: courses = [], isLoading, error: listError } = useQuery({
    queryKey: ['courses', filterCreatorId],
    queryFn: () => getCourses(filterCreatorId || undefined),
  })

  const createMutation = useMutation({
    mutationFn: createCourse,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['courses'] })
      setId('')
      setCreatorId('')
      setTitle('')
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ courseId, title }: { courseId: string; title: string }) =>
      updateCourseTitle(courseId, title),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['courses'] })
      setEditingId(null)
      setEditingTitle('')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: deleteCourse,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['courses'] }),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (id && creatorId && title) createMutation.mutate({ id, creatorId, title })
  }

  const startEdit = (courseId: string, currentTitle: string) => {
    setEditingId(courseId)
    setEditingTitle(currentTitle)
  }

  return (
    <div>
      <h1>강좌 관리</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: '1.5rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        <input
          placeholder="강좌 ID"
          value={id}
          onChange={(e) => setId(e.target.value)}
          required
        />
        <input
          placeholder="크리에이터 ID"
          value={creatorId}
          onChange={(e) => setCreatorId(e.target.value)}
          required
        />
        <input
          placeholder="강좌명"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
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

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <input
          placeholder="크리에이터 ID로 필터"
          value={filterCreatorId}
          onChange={(e) => setFilterCreatorId(e.target.value)}
          style={{ padding: '0.4rem' }}
        />
      </div>

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
              <th style={th}>크리에이터 ID</th>
              <th style={th}>강좌명</th>
              <th style={th}>작업</th>
            </tr>
          </thead>
          <tbody>
            {courses.length === 0 ? (
              <tr>
                <td colSpan={4} style={{ ...td, textAlign: 'center' }}>등록된 강좌 없음</td>
              </tr>
            ) : (
              courses.map((c) => (
                <tr key={c.id}>
                  <td style={td}>{c.id}</td>
                  <td style={td}>{c.creatorId}</td>
                  <td style={td}>
                    {editingId === c.id ? (
                      <span style={{ display: 'flex', gap: '0.4rem' }}>
                        <input
                          value={editingTitle}
                          onChange={(e) => setEditingTitle(e.target.value)}
                          style={{ padding: '0.2rem' }}
                        />
                        <button
                          onClick={() => updateMutation.mutate({ courseId: c.id, title: editingTitle })}
                          disabled={updateMutation.isPending}
                        >
                          저장
                        </button>
                        <button onClick={() => setEditingId(null)}>취소</button>
                      </span>
                    ) : (
                      c.title
                    )}
                  </td>
                  <td style={td}>
                    {editingId !== c.id && (
                      <button onClick={() => startEdit(c.id, c.title)} style={{ marginRight: '0.4rem' }}>
                        수정
                      </button>
                    )}
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

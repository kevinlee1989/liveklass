import apiClient from './client'

export interface Creator {
  id: string
  name: string
}

export const getCreators = () =>
  apiClient.get<Creator[]>('/creators').then((r) => r.data)

export const createCreator = (data: { id: string; name: string }) =>
  apiClient.post<{ id: string }>('/creators', data).then((r) => r.data)

export const deleteCreator = (creatorId: string) =>
  apiClient.delete(`/creators/${creatorId}`)

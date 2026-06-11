import apiClient from './client'

export interface SaleRecord {
  id: string
  creatorId: string
  courseId: string
  courseTitle: string
  studentId: string
  amount: number
  paidAt: string
}

export const getSaleRecords = (creatorId: string, from?: string, to?: string) =>
  apiClient
    .get<SaleRecord[]>('/sale-records', { params: { creatorId, from, to } })
    .then((r) => r.data)

export const createSaleRecord = (data: {
  creatorId: string
  courseId: string
  saleAmount: number
  saleDate: string
}) => apiClient.post<{ id: string }>('/sale-records', data).then((r) => r.data)

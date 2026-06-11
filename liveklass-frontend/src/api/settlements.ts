import apiClient from './client'

export interface MonthlySettlement {
  creatorId: string
  month: string
  status: 'PENDING' | 'CONFIRMED' | 'PAID'
  totalSaleAmount: number
  totalRefundAmount: number
  netAmount: number
  commissionRate: number
  commissionAmount: number
  settlementAmount: number
}

export interface SettlementSummary {
  from: string
  to: string
  totalSettlementAmount: number
  paidAmount: number
  pendingAmount: number
}

export const getCreatorSettlement = (creatorId: string, month: string) =>
  apiClient
    .get<MonthlySettlement>(`/settlements/creators/${creatorId}`, { params: { month } })
    .then((r) => r.data)

export const updateSettlementStatus = (
  creatorId: string,
  month: string,
  status: 'CONFIRMED' | 'PAID'
) =>
  apiClient
    .patch<MonthlySettlement>(`/settlements/creators/${creatorId}`, { status }, { params: { month } })
    .then((r) => r.data)

export const getSettlementSummary = (from: string, to: string) =>
  apiClient
    .get<SettlementSummary>('/settlements/summary', { params: { from, to } })
    .then((r) => r.data)

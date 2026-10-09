import type { Metrics, Order, Question, QuestionStatus } from '../types'

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, { headers: { 'Content-Type': 'application/json', ...options?.headers }, ...options })
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: 'No pudimos completar la solicitud' }))
    throw new Error(body.message)
  }
  return response.json()
}

export const api = {
  orders: (sellerId?: string) => request<Order[]>(`/api/orders${sellerId ? `?sellerId=${sellerId}` : ''}`),
  questions: (sellerId?: string, status?: QuestionStatus) => {
    const params = new URLSearchParams()
    if (sellerId) params.set('sellerId', sellerId)
    if (status) params.set('status', status)
    return request<Question[]>(`/api/questions?${params}`)
  },
  priorityQueue: () => request<Question[]>('/api/operations/questions'),
  metrics: () => request<Metrics>('/api/operations/metrics'),
  answer: (id: string, answer: string) => request<Question>(`/api/questions/${id}/answer`, { method: 'POST', body: JSON.stringify({ answer }) }),
  reset: () => request<{ message: string }>('/api/demo/reset', { method: 'POST' })
}

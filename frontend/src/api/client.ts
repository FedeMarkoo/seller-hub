import type { Metrics, Order, Page, Question, QuestionGroup, QuestionGroupAnalysis, QuestionGroupPage, QuestionStatus, OrderStatus } from '../types'

async function request<T>(url: string, options?: RequestInit, token?: string): Promise<T> {
  const headers = new Headers(options?.headers)
  headers.set('Content-Type', 'application/json')
  if (token) headers.set('X-Demo-Session', token)
  const response = await fetch(url, { ...options, headers })
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: 'No pudimos completar la solicitud' }))
    throw new Error(body.message)
  }
  return response.json()
}

export const api = {
  session: (role: 'SELLER' | 'OPERATIONS', sellerId?: string) => request<{ token: string }>(
    '/api/demo/session',
    { method: 'POST', body: JSON.stringify({ role, sellerId }) },
  ),
  orders: (token: string, sellerId?: string, signal?: AbortSignal, page = 0, search = '', status?: OrderStatus, from?: string, to?: string) => {
    const params = new URLSearchParams()
    if (sellerId) params.set('sellerId', sellerId)
    params.set('page', String(page)); params.set('pageSize', '100')
    if (search.trim()) params.set('search', search.trim())
    if (status) params.set('status', status)
    if (from) params.set('from', from)
    if (to) params.set('to', to)
    return request<Page<Order>>(`/api/orders?${params}`, { signal }, token)
  },
  questions: (token: string, sellerId?: string, status?: QuestionStatus, signal?: AbortSignal, orderId?: string) => {
    const params = new URLSearchParams()
    if (sellerId) params.set('sellerId', sellerId)
    if (status) params.set('status', status)
    if (orderId) params.set('orderId', orderId)
    params.set('page', '0'); params.set('pageSize', '100')
    return request<Page<Question>>(`/api/questions?${params}`, { signal }, token)
  },
  questionGroups: (token: string, sellerId?: string, status?: QuestionStatus, signal?: AbortSignal, page = 0, pageSize = 10, search = '') => {
    const params = new URLSearchParams()
    if (sellerId) params.set('sellerId', sellerId)
    if (status) params.set('status', status)
    params.set('page', String(page)); params.set('pageSize', String(pageSize))
    if (search.trim()) params.set('search', search.trim())
    return request<QuestionGroupPage>(`/api/questions/groups?${params}`, { signal }, token)
  },
  analyzeGroups: (token: string) => request<QuestionGroupAnalysis>('/api/questions/groups/analyze', { method: 'POST' }, token),
  analysisStatus: (token: string) => request<QuestionGroupAnalysis>('/api/questions/groups/analyze/status', undefined, token),
  questionGroup: (token: string, id: string, sellerId?: string, signal?: AbortSignal, status?: QuestionStatus, search = '') => {
    const params = new URLSearchParams()
    if (sellerId) params.set('sellerId', sellerId)
    if (status) params.set('status', status)
    if (search.trim()) params.set('search', search.trim())
    return request<QuestionGroup>(`/api/questions/groups/${encodeURIComponent(id)}${params.toString() ? `?${params}` : ''}`, { signal }, token)
  },
  priorityQueue: (token: string, signal?: AbortSignal, page = 0, pageSize = 10) => request<Page<Question>>(
    `/api/operations/questions?page=${page}&pageSize=${pageSize}`,
    { signal },
    token,
  ),
  metrics: (token: string, signal?: AbortSignal) => request<Metrics>('/api/operations/metrics', { signal }, token),
  answer: (token: string, id: string, answer: string) => request<Question>(
    `/api/questions/${encodeURIComponent(id)}/answer`,
    { method: 'POST', body: JSON.stringify({ answer }) },
    token,
  ),
  transitionOrder: (token: string, id: string, status: OrderStatus) => request<Order>(
    `/api/orders/${encodeURIComponent(id)}/status`, { method: 'POST', body: JSON.stringify({ status }) }, token,
  ),
  createQuestion: (token: string, orderId: string, buyerName: string, text: string) => request<Question>(
    `/api/orders/${encodeURIComponent(orderId)}/questions`, { method: 'POST', body: JSON.stringify({ buyerName, text }) }, token,
  ),
  logout: (token: string) => request<void>('/api/demo/session', { method: 'DELETE' }, token),
  reset: (token: string) => request<{ message: string }>('/api/demo/reset', { method: 'POST' }, token)
}

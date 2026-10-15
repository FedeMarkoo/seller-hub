export type OrderStatus = 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
export type QuestionStatus = 'PENDING' | 'ANSWERED'
export type QuestionGroupAnalysisStatus = 'IDLE' | 'RUNNING' | 'COMPLETED' | 'FAILED'
export type Priority = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export interface OrderItem {
  productId?: string
  product: string
  quantity: number
  unitPrice: number
}
export interface Order {
  id: string
  sellerId: string
  buyerName: string
  createdAt: string
  status: OrderStatus
  items: OrderItem[]
  total: number
  pendingQuestions: number
}
export interface Question {
  id: string
  orderId: string
  sellerId: string
  buyerName: string
  text: string
  createdAt: string
  status: QuestionStatus
  answer?: string
  answeredAt?: string
  priority: Priority
  waitingHours: number
}
export interface QuestionGroup {
  id: string
  representativeText: string
  questions: Question[]
  questionCount: number
  pendingCount: number
}
export interface Metrics {
  totalOrders: number
  grossSales: number
  totalQuestions: number
  answeredQuestions: number
  pendingQuestions: number
  criticalQuestions: number
  ordersWithPendingQuestions: number
  processingOrders: number
  shippedOrders: number
  averageResponseHours: number
  answerRate: number
  questionsByPriority: Record<Priority, number>
  ordersByStatus: Record<string, number>
  topProducts: { product: string; purchaseCount: number; questionCount: number; unitsSold: number }[]
  topBuyers: { buyerName: string; purchaseCount: number; questionCount: number }[]
  mostCommonQuestion: { text: string; questionCount: number } | null
  trend: {
    periodDays: number
    currentOrders: number
    previousOrders: number
    currentRevenue: number
    previousRevenue: number
    ordersChangePercent: number
    revenueChangePercent: number
  }
}
export interface Page<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
  hasNext: boolean
}
export interface QuestionGroupAnalysis {
  status: QuestionGroupAnalysisStatus
  startedAt?: string
  finishedAt?: string
  error?: string
  groups: number
}
export interface QuestionGroupPage {
  items: QuestionGroup[]
  page: number
  pageSize: number
  totalGroups: number
  totalQuestions: number
  pendingQuestions: number
  hasNext: boolean
  analyzed: boolean
  analysis: QuestionGroupAnalysis
}

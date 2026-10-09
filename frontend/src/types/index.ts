export type OrderStatus = 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
export type QuestionStatus = 'PENDING' | 'ANSWERED'
export type Priority = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export interface OrderItem { product: string; quantity: number; unitPrice: number }
export interface Order {
  id: string; sellerId: string; buyerName: string; buyerEmail: string; createdAt: string;
  status: OrderStatus; shippingAddress: string; items: OrderItem[]; total: number; pendingQuestions: number
}
export interface Question {
  id: string; orderId: string; sellerId: string; buyerName: string; text: string; createdAt: string;
  status: QuestionStatus; answer?: string; answeredAt?: string; priority: Priority; waitingHours: number
}
export interface Metrics {
  totalOrders: number; grossSales: number; pendingQuestions: number; criticalQuestions: number;
  averageResponseHours: number; answerRate: number; questionsByPriority: Record<Priority, number>;
  ordersByStatus: Record<string, number>
}

import type { OrderStatus, Priority } from '../types'

const orderLabels: Record<OrderStatus, string> = {
  PROCESSING: 'En preparación', SHIPPED: 'Enviado', DELIVERED: 'Entregado', CANCELLED: 'Cancelado'
}
const priorityLabels: Record<Priority, string> = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }

export function OrderBadge({ status }: { status: OrderStatus }) {
  return <span className={`badge badge--${status.toLowerCase()}`}>{orderLabels[status]}</span>
}
export function PriorityBadge({ priority }: { priority: Priority }) {
  return <span className={`badge badge--${priority.toLowerCase()}`}><i />{priorityLabels[priority]}</span>
}

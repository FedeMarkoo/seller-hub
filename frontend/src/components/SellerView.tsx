import { CircleDollarSign, MessageCircleQuestion, Search, ShoppingBag, CheckCircle2 } from 'lucide-react'
import type { Order, Question, OrderStatus } from '../types'
import { MetricCard } from './MetricCard'
import { OrderBadge } from './StatusBadge'

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 })
const date = new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: 'short', year: 'numeric' })

interface Props {
  orders: Order[]
  questions: Question[]
  pending: Question[]
  query: string
  onQueryChange: (value: string) => void
  onOpenOrder: (order: Order) => void
  status?: OrderStatus
  from?: string
  to?: string
  onStatusChange: (value?: OrderStatus) => void
  onFromChange: (value: string) => void
  onToChange: (value: string) => void
}

export function SellerView({ orders, questions, pending, query, onQueryChange, onOpenOrder, status, from = '', to = '', onStatusChange, onFromChange, onToChange }: Props) {
  const sales = orders.reduce((sum, order) => sum + order.total, 0)
  const answered = questions.filter(question => question.status === 'ANSWERED').length

  return (
    <>
      <section className="hero">
        <div>
          <p className="eyebrow">RESUMEN DE HOY</p>
          <h2>Todo tu negocio,<br /><em>en un solo lugar.</em></h2>
          <p>Seguí cada pedido y respondé a tus compradores antes de que una duda se transforme en un problema.</p>
        </div>
        <div className="hero__art">
          <ShoppingBag size={72} />
          <span className="float-card"><MessageCircleQuestion size={17} />{pending.length} por responder</span>
        </div>
      </section>

      <section className="metrics metrics--three">
        <MetricCard label="Ventas visibles" value={money.format(sales)} detail={`${orders.length} pedidos del vendedor`} icon={CircleDollarSign} tone="green" />
        <MetricCard
          label="Preguntas pendientes"
          value={String(pending.length)}
          detail={pending.length ? 'Requieren tu atención' : 'Todo al día'}
          icon={MessageCircleQuestion}
          tone="orange"
        />
        <MetricCard label="Respuestas enviadas" value={String(answered)} detail="En este conjunto demo" icon={CheckCircle2} />
      </section>

      <section className="panel">
        <div className="panel__header">
          <div><p className="eyebrow">ACTIVIDAD RECIENTE</p><h2>Pedidos</h2></div>
          <label className="search">
            <Search size={17} />
            <input value={query} onChange={event => onQueryChange(event.target.value)} placeholder="Buscar pedido o comprador" />
          </label>
          <label className="filter-control"><span>Estado</span><select value={status ?? ''} onChange={event => onStatusChange((event.target.value || undefined) as OrderStatus | undefined)}><option value="">Todos</option><option value="PROCESSING">En preparación</option><option value="SHIPPED">Enviado</option><option value="DELIVERED">Entregado</option><option value="CANCELLED">Cancelado</option></select></label>
          <label className="filter-control"><span>Desde</span><input type="date" value={from} onChange={event => onFromChange(event.target.value)} /></label>
          <label className="filter-control"><span>Hasta</span><input type="date" value={to} onChange={event => onToChange(event.target.value)} /></label>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Pedido</th><th>Comprador</th><th>Productos</th><th>Total</th><th>Estado</th><th>Consultas</th></tr></thead>
            <tbody>
              {orders.length ? orders.map(order => (
                <tr
                  key={order.id}
                  className="order-row"
                  tabIndex={0}
                  role="button"
                  onClick={() => onOpenOrder(order)}
                  onKeyDown={event => {
                    if (event.key === 'Enter' || event.key === ' ') {
                      event.preventDefault()
                      onOpenOrder(order)
                    }
                  }}
                >
                  <td><strong>{order.id}</strong><small>{date.format(new Date(order.createdAt))}</small></td>
                  <td>{order.buyerName}</td>
                  <td>{order.items[0]?.product ?? 'Sin productos'}{order.items.length > 1 && <small> +{order.items.length - 1} producto</small>}</td>
                  <td><strong>{money.format(order.total)}</strong></td>
                  <td><OrderBadge status={order.status} /></td>
                  <td>{order.pendingQuestions ? <span className="question-count">{order.pendingQuestions}</span> : <span className="muted">—</span>}</td>
                </tr>
              )) : <tr><td colSpan={6} className="empty-state">No hay pedidos para mostrar.</td></tr>}
            </tbody>
          </table>
        </div>
      </section>
    </>
  )
}

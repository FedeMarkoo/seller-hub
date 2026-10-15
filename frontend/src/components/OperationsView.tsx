import { ArrowRight, BarChart3, CheckCircle2, CircleDollarSign, Clock3, Inbox, MessageCircleQuestion, ShoppingBag, Truck } from 'lucide-react'
import type { ReactNode } from 'react'
import type { Metrics, Order, Question } from '../types'
import { MetricCard } from './MetricCard'
import { Pagination } from './Pagination'
import { PriorityBadge } from './StatusBadge'

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 })
const priorityLabels: Record<string, string> = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }

interface Props {
  metrics: Metrics
  questions: Question[]
  orders: Order[]
  total: number
  page: number
  pageSize: number
  hasNext: boolean
  onPage: (page: number) => void
  onPageSize: (size: number) => void
  onAnswer: (question: Question) => void
}

export function OperationsView({ metrics, questions, orders, total, page, pageSize, hasNext, onPage, onPageSize, onAnswer }: Props) {
  const maxPriority = Math.max(...Object.values(metrics.questionsByPriority), 1)

  return (
    <>
      <section className="operations-intro">
        <div>
          <p className="eyebrow">OPERACIONES · TIEMPO REAL</p>
          <h2>Lo urgente primero.</h2>
          <p>Una lectura rápida de la salud del canal y las conversaciones que necesitan intervención.</p>
        </div>
        <span className="live"><i /> DATOS EN VIVO</span>
      </section>
      <section className="metrics metrics--four">
        <MetricCard
          label="Preguntas pendientes"
          value={String(metrics.pendingQuestions)}
          detail={`${metrics.criticalQuestions} fuera del SLA`}
          icon={MessageCircleQuestion}
          tone="orange"
        />
        <MetricCard label="Tasa de respuesta" value={`${metrics.answerRate}%`} detail="Sobre todas las consultas" icon={CheckCircle2} tone="green" />
        <MetricCard label="Tiempo medio" value={`${metrics.averageResponseHours} h`} detail="Hasta la primera respuesta" icon={Clock3} />
        <MetricCard label="Ventas brutas" value={money.format(metrics.grossSales)} detail={`${metrics.totalOrders} pedidos totales`} icon={CircleDollarSign} />
        <MetricCard label="Preguntas totales" value={String(metrics.totalQuestions)} detail={`${metrics.answeredQuestions} respondidas`} icon={Inbox} />
        <MetricCard
          label="Pedidos con consultas"
          value={String(metrics.ordersWithPendingQuestions)}
          detail="Con al menos una pendiente"
          icon={MessageCircleQuestion}
          tone="orange"
        />
        <MetricCard label="Pedidos en preparación" value={String(metrics.processingOrders)} detail="Estado operativo actual" icon={ShoppingBag} />
        <MetricCard label="Pedidos en camino" value={String(metrics.shippedOrders)} detail="Despachados a logística" icon={Truck} tone="green" />
      </section>
      <section className="panel trend-panel">
        <div className="panel__header"><div><p className="eyebrow">TENDENCIA · ÚLTIMOS {metrics.trend.periodDays} DÍAS</p><h2>Comparativa operativa</h2></div><ArrowRight size={19} /></div>
        <div className="trend-grid">
          <div><span>Pedidos</span><strong>{metrics.trend.currentOrders}</strong><small>{formatChange(metrics.trend.ordersChangePercent)} vs período anterior</small></div>
          <div><span>Facturación</span><strong>{money.format(metrics.trend.currentRevenue)}</strong><small>{formatChange(metrics.trend.revenueChangePercent)} vs período anterior</small></div>
        </div>
      </section>
      <section className="insights-grid">
        <InsightPanel title="Top de productos" eyebrow="DEMANDA" icon={<ShoppingBag size={19} />}>
          {metrics.topProducts.map((product, index) => (
            <InsightRow
              key={product.product}
              rank={index + 1}
              title={product.product}
              detail={`${product.questionCount} preguntas · ${product.unitsSold} unidades`}
              value={`${product.purchaseCount} compras`}
            />
          ))}
        </InsightPanel>
        <InsightPanel title="Top de compradores" eyebrow="CLIENTES" icon={<MessageCircleQuestion size={19} />}>
          {metrics.topBuyers.map((buyer, index) => (
            <InsightRow
              key={buyer.buyerName}
              rank={index + 1}
              title={buyer.buyerName}
              detail={`${buyer.questionCount} preguntas`}
              value={`${buyer.purchaseCount} compras`}
            />
          ))}
        </InsightPanel>
        <section className="panel common-question">
          <div className="panel__header">
            <div>
              <p className="eyebrow">OPORTUNIDAD DE AUTOMATIZACIÓN</p>
              <h2>Pregunta más común</h2>
            </div>
            <Inbox size={19} />
          </div>
          {metrics.mostCommonQuestion ? (
            <div className="common-question__body">
              <p>“{metrics.mostCommonQuestion.text}”</p>
              <strong>{metrics.mostCommonQuestion.questionCount} consultas agrupadas</strong>
              <small>Es el tema con mayor volumen de preguntas similares.</small>
            </div>
          ) : <p className="empty-state">No hay preguntas para analizar.</p>}
        </section>
      </section>
      <div className="operations-grid">
        <section className="panel priority-panel">
          <div className="panel__header">
            <div>
              <p className="eyebrow">COLA PRIORIZADA</p>
              <h2>Preguntas pendientes</h2>
            </div>
            <span className="counter">Ordenadas por SLA</span>
          </div>
          <div className="priority-list">
            {questions.map((question, index) => (
              <PriorityQuestion
                key={question.id}
                question={question}
                rank={page * pageSize + index + 1}
                onAnswer={onAnswer}
              />
            ))}
          </div>
          <Pagination
            page={page}
            pageSize={pageSize}
            hasNext={hasNext}
            onPage={onPage}
            onPageSize={onPageSize}
            label={`Mostrando ${questions.length} de ${total} pendientes`}
          />
        </section>
        <aside className="side-stack">
          <section className="panel chart">
            <div className="panel__header">
              <div>
                <p className="eyebrow">DISTRIBUCIÓN</p>
                <h2>Por prioridad</h2>
              </div>
              <BarChart3 size={20} />
            </div>
            {Object.entries(metrics.questionsByPriority).map(([key, value]) => (
              <div className="bar-row" key={key}>
                <span>{priorityLabels[key]}</span>
                <div>
                  <i
                    style={{ width: `${value / maxPriority * 100}%` }}
                    className={`bar bar--${key.toLowerCase()}`}
                  />
                </div>
                <strong>{value}</strong>
              </div>
            ))}
          </section>
          <section className="panel shipment">
            <div className="shipment__icon"><Truck size={26} /></div>
            <p className="eyebrow">LOGÍSTICA</p>
            <h3>{orders.filter(order => order.status === 'SHIPPED').length} pedidos en camino</h3>
            <p>La operación mantiene {orders.filter(order => order.status === 'PROCESSING').length} pedidos en preparación.</p>
          </section>
        </aside>
      </div>
    </>
  )
}

function formatChange(value: number) {
  return `${value >= 0 ? '+' : ''}${value}%`
}

function InsightPanel({ title, eyebrow, icon, children }: { title: string; eyebrow: string; icon: ReactNode; children: ReactNode }) {
  return <section className="panel insight-panel">
    <div className="panel__header"><div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2></div>{icon}</div>
    <div className="insight-list">{children}</div>
  </section>
}

function InsightRow({ rank, title, detail, value }: { rank: number; title: string; detail: string; value: string }) {
  return <article>
    <span className="insight-rank">{String(rank).padStart(2, '0')}</span>
    <div><strong>{title}</strong><small>{detail}</small></div>
    <b>{value}</b>
  </article>
}

function PriorityQuestion({ question, rank, onAnswer }: { question: Question; rank: number; onAnswer: (question: Question) => void }) {
  return <article>
    <span className="rank">{String(rank).padStart(2, '0')}</span>
    <div className="priority-main">
      <div><PriorityBadge priority={question.priority} /><small>{question.orderId} · {question.buyerName}</small></div>
      <p>{question.text}</p>
      <span><Clock3 size={14} /> Esperando hace {question.waitingHours} horas</span>
    </div>
    <button className="icon-button" onClick={() => onAnswer(question)} aria-label="Responder"><ArrowRight size={18} /></button>
  </article>
}

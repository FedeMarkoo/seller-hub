import { useEffect, useRef, useState, type FormEvent } from 'react'
import { MessageCircleQuestion, Package, X } from 'lucide-react'
import type { Order, Question, OrderStatus } from '../types'
import { OrderBadge } from './StatusBadge'

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 })
const date = new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' })

interface Props {
  order: Order
  questions: Question[]
  loadingQuestions: boolean
  onClose: () => void
  onAnswer: (question: Question) => void
  onTransition: (status: OrderStatus) => Promise<void>
  onAsk: (buyerName: string, text: string) => Promise<void>
}

export function OrderDetailsModal({ order, questions, loadingQuestions, onClose, onAnswer, onTransition, onAsk }: Props) {
  const closeButton = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    const previousFocus = document.activeElement as HTMLElement | null
    closeButton.current?.focus()
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      previousFocus?.focus()
    }
  }, [onClose])

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="modal order-modal" role="dialog" aria-modal="true" aria-labelledby="order-title" onMouseDown={event => event.stopPropagation()}>
        <button ref={closeButton} className="icon-button modal__close" onClick={onClose} aria-label="Cerrar detalle del pedido"><X size={20} /></button>
        <div className="modal__icon"><Package size={22} /></div>
        <OrderHeading order={order} />
        <OrderActions order={order} onTransition={onTransition} onAsk={onAsk} />
        <OrderSummary order={order} />
        <OrderItems order={order} />
        <OrderQuestions questions={questions} loading={loadingQuestions} onAnswer={onAnswer} />
      </section>
    </div>
  )
}

function OrderActions({ order, onTransition, onAsk }: { order: Order; onTransition: (status: OrderStatus) => Promise<void>; onAsk: (buyerName: string, text: string) => Promise<void> }) {
  const [status, setStatus] = useState<OrderStatus>(order.status)
  const [text, setText] = useState('')
  const [saving, setSaving] = useState(false)
  const canMove = (next: OrderStatus) => (order.status === 'PROCESSING' && ['SHIPPED', 'CANCELLED'].includes(next)) || (order.status === 'SHIPPED' && ['DELIVERED', 'CANCELLED'].includes(next))
  async function submitQuestion(event: FormEvent) {
    event.preventDefault(); if (!text.trim()) return
    setSaving(true); try { await onAsk(order.buyerName, text.trim()); setText('') } finally { setSaving(false) }
  }
  return <div className="order-actions">
    <label>Actualizar estado <select value={status} onChange={event => setStatus(event.target.value as OrderStatus)} disabled={!canMove(status) || saving}>
      <option value={order.status}>{order.status}</option><option value="SHIPPED" disabled={!canMove('SHIPPED')}>SHIPPED</option><option value="DELIVERED" disabled={!canMove('DELIVERED')}>DELIVERED</option><option value="CANCELLED" disabled={!canMove('CANCELLED')}>CANCELLED</option>
    </select></label>
    {status !== order.status && <button className="button button--secondary" disabled={saving} onClick={() => onTransition(status)}>Guardar estado</button>}
    <form onSubmit={submitQuestion}><label>Simular pregunta del comprador <textarea value={text} maxLength={2000} onChange={event => setText(event.target.value)} placeholder="Escribí una consulta…" /></label><button className="button button--secondary" disabled={saving || !text.trim()}>Enviar pregunta</button></form>
  </div>
}

function OrderHeading({ order }: { order: Order }) {
  return (
    <div className="order-modal__heading">
      <div>
        <p className="eyebrow">DETALLE DEL PEDIDO</p>
        <h2 id="order-title">{order.id}</h2>
      </div>
      <OrderBadge status={order.status} />
    </div>
  )
}

function OrderSummary({ order }: { order: Order }) {
  return (
    <div className="order-summary">
      <div><span>Comprador</span><strong>{order.buyerName}</strong></div>
      <div><span>Creado</span><strong>{date.format(new Date(order.createdAt))}</strong></div>
      <div><span>Total</span><strong>{money.format(order.total)}</strong></div>
    </div>
  )
}

function OrderItems({ order }: { order: Order }) {
  return (
    <div className="order-modal__section">
      <h3>Productos</h3>
      <div className="order-items">
        {order.items.map(item => (
          <div className="order-item" key={`${item.product}-${item.unitPrice}`}>
            <span>{item.product}</span>
            <small>{item.quantity} × {money.format(item.unitPrice)}</small>
            <strong>{money.format(item.quantity * item.unitPrice)}</strong>
          </div>
        ))}
      </div>
    </div>
  )
}

function OrderQuestions({ questions, loading, onAnswer }: { questions: Question[]; loading: boolean; onAnswer: (question: Question) => void }) {
  return (
    <div className="order-modal__section">
      <div className="order-questions-heading"><h3>Preguntas de este pedido</h3><span className="counter">{questions.length}</span></div>
      {loading ? <p className="modal-muted">Cargando preguntas…</p> : questions.length ? (
        <div className="order-questions">
          {questions.map(question => <OrderQuestion key={question.id} question={question} onAnswer={onAnswer} />)}
        </div>
      ) : <p className="modal-muted">Este pedido no tiene preguntas.</p>}
    </div>
  )
}

function OrderQuestion({ question, onAnswer }: { question: Question; onAnswer: (question: Question) => void }) {
  return (
    <article className="order-question">
      <div>
        <MessageCircleQuestion size={16} />
        <div>
          <strong>{question.buyerName}</strong>
          <p>{question.text}</p>
          {question.answer && <small>Respuesta: {question.answer}</small>}
        </div>
      </div>
      {question.status === 'PENDING' ? (
        <button className="button button--secondary" onClick={() => onAnswer(question)}>
          Responder
        </button>
      ) : <span className="answered-label">Respondida</span>}
    </article>
  )
}

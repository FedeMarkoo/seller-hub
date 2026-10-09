import { useCallback, useEffect, useMemo, useState } from 'react'
import { AlertCircle, ArrowRight, BarChart3, Boxes, CheckCircle2, ChevronDown, CircleDollarSign, Clock3, Headphones, LayoutDashboard, MessageCircleQuestion, RefreshCcw, Search, ShoppingBag, Store, Truck } from 'lucide-react'
import { api } from './api/client'
import { AnswerModal } from './components/AnswerModal'
import { MetricCard } from './components/MetricCard'
import { OrderBadge, PriorityBadge } from './components/StatusBadge'
import type { Metrics, Order, Question } from './types'

type View = 'seller' | 'operations'
const sellers = [{ id: 'seller-01', name: 'Tecno Sur' }, { id: 'seller-02', name: 'Casa Nativa' }, { id: 'seller-03', name: 'Lumen' }]
const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 })
const date = new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: 'short', year: 'numeric' })

function App() {
  const [view, setView] = useState<View>('seller')
  const [sellerId, setSellerId] = useState('seller-01')
  const [orders, setOrders] = useState<Order[]>([])
  const [questions, setQuestions] = useState<Question[]>([])
  const [metrics, setMetrics] = useState<Metrics | null>(null)
  const [selected, setSelected] = useState<Question | null>(null)
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')

  const load = useCallback(async () => {
    setLoading(true); setError('')
    try {
      if (view === 'seller') {
        const [nextOrders, nextQuestions] = await Promise.all([api.orders(sellerId), api.questions(sellerId)])
        setOrders(nextOrders); setQuestions(nextQuestions)
      } else {
        const [nextQuestions, nextMetrics, allOrders] = await Promise.all([api.priorityQueue(), api.metrics(), api.orders()])
        setQuestions(nextQuestions); setMetrics(nextMetrics); setOrders(allOrders)
      }
    } catch (e) { setError(e instanceof Error ? e.message : 'Error inesperado') }
    finally { setLoading(false) }
  }, [sellerId, view])

  useEffect(() => { void load() }, [load])

  const filteredOrders = useMemo(() => orders.filter(o => `${o.id} ${o.buyerName} ${o.items.map(i => i.product)}`.toLowerCase().includes(query.toLowerCase())), [orders, query])
  const pending = questions.filter(q => q.status === 'PENDING')

  async function submitAnswer(answer: string) {
    if (!selected) return
    setSaving(true)
    try { await api.answer(selected.id, answer); setSelected(null); await load() }
    catch (e) { setError(e instanceof Error ? e.message : 'No se pudo enviar la respuesta') }
    finally { setSaving(false) }
  }

  async function resetDemo() { await api.reset(); await load() }

  return <div className="shell">
    <aside className="sidebar">
      <a className="brand" href="#top"><span><Boxes size={23} /></span><strong>Nexo</strong></a>
      <nav>
        <p>ESPACIO DE TRABAJO</p>
        <button className={view === 'seller' ? 'active' : ''} onClick={() => setView('seller')}><Store size={19} />Vendedores</button>
        <button className={view === 'operations' ? 'active' : ''} onClick={() => setView('operations')}><LayoutDashboard size={19} />Operaciones</button>
      </nav>
      <div className="sidebar__support"><Headphones size={20} /><div><strong>¿Necesitás ayuda?</strong><small>Centro de soporte</small></div><ArrowRight size={16} /></div>
      <div className="profile"><span>FM</span><div><strong>Federico M.</strong><small>Administrador</small></div><ChevronDown size={15} /></div>
    </aside>

    <main id="top">
      <header className="topbar">
        <div><p className="eyebrow">CENTRO DE VENTAS</p><h1>{view === 'seller' ? 'Pedidos y consultas' : 'Pulso operativo'}</h1></div>
        <div className="topbar__actions">
          {view === 'seller' && <label className="seller-select"><Store size={16} /><select value={sellerId} onChange={e => setSellerId(e.target.value)}>{sellers.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}</select><ChevronDown size={15} /></label>}
          <button className="button button--ghost" onClick={resetDemo}><RefreshCcw size={16} />Reiniciar demo</button>
        </div>
      </header>

      <div className="content">
        {error && <div className="error"><AlertCircle size={18} />{error}<button onClick={() => setError('')}>×</button></div>}
        {loading ? <div className="loader"><span /><p>Actualizando información…</p></div> : view === 'seller'
          ? <SellerView orders={filteredOrders} questions={questions} pending={pending} query={query} setQuery={setQuery} onAnswer={setSelected} />
          : metrics && <OperationsView metrics={metrics} questions={pending} orders={orders} onAnswer={setSelected} />}
      </div>
    </main>
    {selected && <AnswerModal key={selected.id} question={selected} saving={saving} onClose={() => setSelected(null)} onSubmit={submitAnswer} />}
  </div>
}

function SellerView({ orders, questions, pending, query, setQuery, onAnswer }: { orders: Order[]; questions: Question[]; pending: Question[]; query: string; setQuery: (v: string) => void; onAnswer: (q: Question) => void }) {
  const sales = orders.reduce((sum, order) => sum + order.total, 0)
  return <>
    <section className="hero"><div><p className="eyebrow">RESUMEN DE HOY</p><h2>Todo tu negocio,<br /><em>en un solo lugar.</em></h2><p>Seguí cada pedido y respondé a tus compradores antes de que una duda se transforme en un problema.</p></div><div className="hero__art"><ShoppingBag size={72} /><span className="float-card"><MessageCircleQuestion size={17} />{pending.length} por responder</span></div></section>
    <section className="metrics metrics--three">
      <MetricCard label="Ventas visibles" value={money.format(sales)} detail={`${orders.length} pedidos del vendedor`} icon={CircleDollarSign} tone="green" />
      <MetricCard label="Preguntas pendientes" value={String(pending.length)} detail={pending.length ? 'Requieren tu atención' : 'Todo al día'} icon={MessageCircleQuestion} tone="orange" />
      <MetricCard label="Respuestas enviadas" value={String(questions.filter(q => q.status === 'ANSWERED').length)} detail="En este conjunto demo" icon={CheckCircle2} />
    </section>
    <section className="panel">
      <div className="panel__header"><div><p className="eyebrow">ACTIVIDAD RECIENTE</p><h2>Pedidos</h2></div><label className="search"><Search size={17} /><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Buscar pedido o comprador" /></label></div>
      <div className="table-wrap"><table><thead><tr><th>Pedido</th><th>Comprador</th><th>Productos</th><th>Total</th><th>Estado</th><th>Consultas</th></tr></thead><tbody>{orders.map(o => <tr key={o.id}><td><strong>{o.id}</strong><small>{date.format(new Date(o.createdAt))}</small></td><td>{o.buyerName}</td><td>{o.items[0].product}{o.items.length > 1 && <small> +{o.items.length - 1} producto</small>}</td><td><strong>{money.format(o.total)}</strong></td><td><OrderBadge status={o.status} /></td><td>{o.pendingQuestions ? <span className="question-count">{o.pendingQuestions}</span> : <span className="muted">—</span>}</td></tr>)}</tbody></table></div>
    </section>
    <section className="panel questions-panel"><div className="panel__header"><div><p className="eyebrow">BANDEJA DE ENTRADA</p><h2>Consultas de compradores</h2></div><span className="counter">{pending.length} pendientes</span></div>
      <div className="question-list">{questions.map(q => <article key={q.id} className={q.status === 'ANSWERED' ? 'question answered' : 'question'}><div className="avatar">{q.buyerName.split(' ').map(n => n[0]).slice(0, 2).join('')}</div><div className="question__body"><div><strong>{q.buyerName}</strong><span>{q.orderId} · hace {q.waitingHours} h</span></div><p>{q.text}</p>{q.answer && <small className="answer-preview">Tu respuesta: {q.answer}</small>}</div>{q.status === 'PENDING' ? <button className="button button--secondary" onClick={() => onAnswer(q)}>Responder <ArrowRight size={15} /></button> : <span className="answered-label"><CheckCircle2 size={16} />Respondida</span>}</article>)}</div>
    </section>
  </>
}

function OperationsView({ metrics, questions, orders, onAnswer }: { metrics: Metrics; questions: Question[]; orders: Order[]; onAnswer: (q: Question) => void }) {
  const maxPriority = Math.max(...Object.values(metrics.questionsByPriority), 1)
  return <>
    <section className="operations-intro"><div><p className="eyebrow">OPERACIONES · TIEMPO REAL</p><h2>Lo urgente primero.</h2><p>Una lectura rápida de la salud del canal y las conversaciones que necesitan intervención.</p></div><span className="live"><i /> DATOS EN VIVO</span></section>
    <section className="metrics metrics--four">
      <MetricCard label="Preguntas pendientes" value={String(metrics.pendingQuestions)} detail={`${metrics.criticalQuestions} fuera del SLA`} icon={MessageCircleQuestion} tone="orange" />
      <MetricCard label="Tasa de respuesta" value={`${metrics.answerRate}%`} detail="Sobre todas las consultas" icon={CheckCircle2} tone="green" />
      <MetricCard label="Tiempo medio" value={`${metrics.averageResponseHours} h`} detail="Hasta la primera respuesta" icon={Clock3} />
      <MetricCard label="Ventas brutas" value={money.format(metrics.grossSales)} detail={`${metrics.totalOrders} pedidos totales`} icon={CircleDollarSign} />
    </section>
    <div className="operations-grid">
      <section className="panel priority-panel"><div className="panel__header"><div><p className="eyebrow">COLA PRIORIZADA</p><h2>Preguntas pendientes</h2></div><span className="counter">Ordenadas por SLA</span></div>
        <div className="priority-list">{questions.map((q, i) => <article key={q.id}><span className="rank">{String(i + 1).padStart(2, '0')}</span><div className="priority-main"><div><PriorityBadge priority={q.priority} /><small>{q.orderId} · {q.buyerName}</small></div><p>{q.text}</p><span><Clock3 size={14} /> Esperando hace {q.waitingHours} horas</span></div><button className="icon-button" onClick={() => onAnswer(q)} aria-label="Responder"><ArrowRight size={18} /></button></article>)}</div>
      </section>
      <aside className="side-stack">
        <section className="panel chart"><div className="panel__header"><div><p className="eyebrow">DISTRIBUCIÓN</p><h2>Por prioridad</h2></div><BarChart3 size={20} /></div>{Object.entries(metrics.questionsByPriority).map(([key, value]) => <div className="bar-row" key={key}><span>{({ CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' } as Record<string, string>)[key]}</span><div><i style={{ width: `${value / maxPriority * 100}%` }} className={`bar bar--${key.toLowerCase()}`} /></div><strong>{value}</strong></div>)}</section>
        <section className="panel shipment"><div className="shipment__icon"><Truck size={26} /></div><p className="eyebrow">LOGÍSTICA</p><h3>{orders.filter(o => o.status === 'SHIPPED').length} pedidos en camino</h3><p>La operación mantiene {orders.filter(o => o.status === 'PROCESSING').length} pedidos en preparación.</p></section>
      </aside>
    </div>
  </>
}

export default App

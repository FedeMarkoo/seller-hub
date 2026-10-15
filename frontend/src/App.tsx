import { useCallback, useEffect, useRef, useState } from 'react'
import { ArrowRight, Boxes, ChevronDown, Headphones, Inbox, LayoutDashboard, RefreshCcw, Store } from 'lucide-react'
import { api } from './api/client'
import { AnswerModal } from './components/AnswerModal'
import { AppContent } from './components/AppContent'
import { OrderDetailsModal } from './components/OrderDetailsModal'
import type { InboxFilter } from './components/InboxFilters'
import type { Metrics, Order, Question, QuestionGroup, QuestionGroupAnalysis, QuestionStatus, OrderStatus } from './types'

type View = 'seller' | 'inbox' | 'operations'
const sellers = [{ id: 'seller-01', name: 'Tecno Sur' }, { id: 'seller-02', name: 'Casa Nativa' }, { id: 'seller-03', name: 'Lumen' }]

function App() {
  const [view, setView] = useState<View>('seller')
  const [sellerId, setSellerId] = useState('seller-01')
  const [orders, setOrders] = useState<Order[]>([])
  const [questions, setQuestions] = useState<Question[]>([])
  const [questionGroups, setQuestionGroups] = useState<QuestionGroup[]>([])
  const [groupsAnalyzed, setGroupsAnalyzed] = useState(false)
  const [analysis, setAnalysis] = useState<QuestionGroupAnalysis>({ status: 'IDLE', groups: 0 })
  const [questionTotal, setQuestionTotal] = useState(0)
  const [pendingTotal, setPendingTotal] = useState(0)
  const [inboxHasNext, setInboxHasNext] = useState(false)
  const [operationsHasNext, setOperationsHasNext] = useState(false)
  const [inboxPage, setInboxPage] = useState(0)
  const [operationsPage, setOperationsPage] = useState(0)
  const [inboxPageSize, setInboxPageSize] = useState(10)
  const [inboxFilter, setInboxFilter] = useState<InboxFilter>('PENDING')
  const [inboxSearch, setInboxSearch] = useState('')
  const [inboxVisibleTotal, setInboxVisibleTotal] = useState(0)
  const [operationsPageSize, setOperationsPageSize] = useState(10)
  const [metrics, setMetrics] = useState<Metrics | null>(null)
  const [selected, setSelected] = useState<Question | null>(null)
  const [selectedGroup, setSelectedGroup] = useState<Question[]>([])
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)
  const [orderQuestions, setOrderQuestions] = useState<Question[]>([])
  const [orderQuestionsLoading, setOrderQuestionsLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [orderStatus, setOrderStatus] = useState<OrderStatus | undefined>()
  const [orderFrom, setOrderFrom] = useState('')
  const [orderTo, setOrderTo] = useState('')
  const [sessionToken, setSessionToken] = useState('')
  const requestId = useRef(0)
  const orderRequestId = useRef(0)

  const load = useCallback(async (signal?: AbortSignal, preserveSelection = false) => {
    const currentRequest = ++requestId.current
    setLoading(true)
    setError('')
    if (!preserveSelection) {
      setSelected(null)
      setSelectedGroup([])
    }
    setQuestions([])
    setQuestionGroups([])
    if (view === 'operations') setMetrics(null)

    try {
      const session = await api.session(view === 'operations' ? 'OPERATIONS' : 'SELLER', sellerId)
      if (currentRequest !== requestId.current) return
      setSessionToken(session.token)

      if (view === 'seller' || view === 'inbox') {
        const groupStatus: QuestionStatus | undefined = inboxFilter === 'ALL' ? undefined : inboxFilter
        const [nextOrders, nextQuestions, nextGroups, groupTotals] = await Promise.all([
          api.orders(session.token, undefined, signal, 0, query, orderStatus, orderFrom, orderTo),
          api.questions(session.token, undefined, undefined, signal),
          view === 'inbox' ? api.questionGroups(session.token, undefined, groupStatus, signal, inboxPage, inboxPageSize, inboxSearch) : Promise.resolve(null),
          view === 'inbox' ? api.questionGroups(session.token, undefined, undefined, signal, 0, 1) : Promise.resolve(null)
        ])
        setOrders(nextOrders.items)
        setQuestions(nextQuestions.items)
        if (nextGroups && groupTotals) {
          setQuestionGroups(nextGroups.items)
          setInboxVisibleTotal(nextGroups.totalQuestions)
          setQuestionTotal(groupTotals.totalQuestions)
          setPendingTotal(groupTotals.pendingQuestions)
          setInboxHasNext(nextGroups.hasNext)
          setGroupsAnalyzed(nextGroups.analyzed)
          setAnalysis(nextGroups.analysis)
        }
      } else {
        const [nextQuestions, nextMetrics, allOrders] = await Promise.all([
          api.priorityQueue(session.token, signal, operationsPage, operationsPageSize),
          api.metrics(session.token, signal),
          api.orders(session.token, undefined, signal)
        ])
        setQuestions(nextQuestions.items)
        setQuestionGroups([])
        setQuestionTotal(nextQuestions.total)
        setPendingTotal(nextQuestions.total)
        setOperationsHasNext(nextQuestions.hasNext)
        setMetrics(nextMetrics)
        setOrders(allOrders.items)
      }
    } catch (exception) {
      if (exception instanceof DOMException && exception.name === 'AbortError') return
      setOrders([])
      setQuestions([])
      setError(exception instanceof Error ? exception.message : 'No pudimos cargar la información')
    } finally {
      if (currentRequest === requestId.current) setLoading(false)
    }
  }, [inboxFilter, inboxPage, inboxPageSize, inboxSearch, operationsPage, operationsPageSize, orderFrom, orderStatus, orderTo, query, sellerId, view])

  useEffect(() => {
    const controller = new AbortController()
    void load(controller.signal)
    return () => controller.abort()
  }, [load])

  useEffect(() => {
    if (view !== 'inbox' || !sessionToken || analysis.status !== 'RUNNING') return
    const interval = window.setInterval(async () => {
      try {
        const nextAnalysis = await api.analysisStatus(sessionToken)
        setAnalysis(nextAnalysis)
        if (nextAnalysis.status !== 'RUNNING') await load()
      } catch (exception) {
        setError(exception instanceof Error ? exception.message : 'No se pudo consultar el estado del análisis')
      }
    }, 1500)
    return () => window.clearInterval(interval)
  }, [analysis.status, load, sessionToken, view])

  const pending = questions.filter(question => question.status === 'PENDING')

  async function submitAnswer(answer: string) {
    if (!selected) return
    const targetQuestions = selectedGroup.length ? selectedGroup : [selected]
    setSaving(true)
    try {
      const results = await Promise.allSettled(targetQuestions.map(question => api.answer(sessionToken, question.id, answer)))
      await load(undefined, true)
      if (selectedOrder && targetQuestions.some(question => question.orderId === selectedOrder.id)) {
        const refreshed = await api.questions(sessionToken, undefined, undefined, undefined, selectedOrder.id)
        setOrderQuestions(refreshed.items)
      }
      const refreshedGroups = (await api.questionGroups(sessionToken, undefined, 'PENDING', undefined, 0, inboxPageSize, inboxSearch)).items
      const answeredIds = new Set(targetQuestions.filter((_, index) => results[index]?.status === 'fulfilled').map(question => question.id))
      const nextQuestions = await findNextPendingQuestions(refreshedGroups, answeredIds)
      setSelected(nextQuestions[0] ?? null)
      setSelectedGroup(nextQuestions)
      const failed = results.filter(result => result.status === 'rejected').length
      if (failed > 0) setError(`${failed} respuesta${failed === 1 ? '' : 's'} no pudo${failed === 1 ? '' : 'ieron'} guardarse. La cola fue actualizada.`)
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'No se pudo enviar la respuesta')
    } finally {
      setSaving(false)
    }
  }

  async function findNextPendingQuestions(groups: QuestionGroup[], answeredIds: Set<string>) {
    for (const group of groups) {
      const loaded = await api.questionGroup(sessionToken, group.id, undefined, undefined, 'PENDING', inboxSearch)
      const pendingQuestions = loaded.questions.filter(question => question.status === 'PENDING' && !answeredIds.has(question.id))
      if (pendingQuestions.length) return pendingQuestions
    }
    return []
  }

  async function openOrder(order: Order) {
    const currentRequest = ++orderRequestId.current
    setSelectedOrder(order)
    setOrderQuestions(questions.filter(question => question.orderId === order.id))
    setOrderQuestionsLoading(true)
    try {
      const nextQuestions = await api.questions(sessionToken, undefined, undefined, undefined, order.id)
      if (currentRequest === orderRequestId.current) setOrderQuestions(nextQuestions.items)
    } catch (exception) {
      if (currentRequest === orderRequestId.current) setError(exception instanceof Error ? exception.message : 'No se pudieron cargar las preguntas del pedido')
    } finally {
      if (currentRequest === orderRequestId.current) setOrderQuestionsLoading(false)
    }
  }

  function closeOrder() {
    orderRequestId.current += 1
    setSelectedOrder(null)
  }

  function openAnswer(question: Question, group: Question[] = [question]) {
    setSelected(question)
    setSelectedGroup(group.filter(candidate => candidate.status === 'PENDING'))
  }

  async function expandGroup(groupId: string): Promise<QuestionGroup> {
    try {
      const groupStatus: QuestionStatus | undefined = inboxFilter === 'ALL' ? undefined : inboxFilter
      const group = await api.questionGroup(sessionToken, groupId, undefined, undefined, groupStatus, inboxSearch)
      setQuestionGroups(current => current.map(candidate => candidate.id === groupId ? group : candidate))
      return group
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'No se pudo cargar el grupo de preguntas')
      throw exception
    }
  }

  async function analyzeGroups() {
    try {
      const nextAnalysis = await api.analyzeGroups(sessionToken)
      setAnalysis(nextAnalysis)
      await load()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'No se pudieron analizar los grupos')
    }
  }

  async function resetDemo() {
    try {
      await api.reset(sessionToken)
      await load()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'No se pudo reiniciar la demo')
    }
  }

  async function transitionOrder(status: Order['status']) {
    if (!selectedOrder) return
    try {
      const updated = await api.transitionOrder(sessionToken, selectedOrder.id, status)
      setSelectedOrder(updated)
      setOrders(current => current.map(order => order.id === updated.id ? updated : order))
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'No se pudo actualizar el estado')
    }
  }

  async function createOrderQuestion(buyerName: string, text: string) {
    if (!selectedOrder) return
    try {
      const created = await api.createQuestion(sessionToken, selectedOrder.id, buyerName, text)
      setOrderQuestions(current => [created, ...current])
      setOrders(current => current.map(order => order.id === selectedOrder.id ? { ...order, pendingQuestions: order.pendingQuestions + 1 } : order))
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'No se pudo crear la pregunta')
      throw exception
    }
  }

  const changeView = (nextView: View) => {
    setView(nextView)
    setInboxPage(0)
    setOperationsPage(0)
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <a className="brand" href="#top"><span><Boxes size={23} /></span><strong>Nexo</strong></a>
        <nav>
          <p>ESPACIO DE TRABAJO</p>
          <button className={view === 'seller' ? 'active' : ''} onClick={() => changeView('seller')}><Store size={19} />Vendedores</button>
          <button className={view === 'inbox' ? 'active' : ''} onClick={() => changeView('inbox')}><Inbox size={19} />Bandeja de entrada</button>
          <button className={view === 'operations' ? 'active' : ''} onClick={() => changeView('operations')}><LayoutDashboard size={19} />Operaciones</button>
        </nav>
        <div className="sidebar__support"><Headphones size={20} /><div><strong>¿Necesitás ayuda?</strong><small>Centro de soporte</small></div><ArrowRight size={16} /></div>
        <div className="profile"><span>FM</span><div><strong>Federico M.</strong><small>Administrador</small></div><ChevronDown size={15} /></div>
      </aside>

      <main id="top">
        <header className="topbar">
          <div>
            <p className="eyebrow">CENTRO DE VENTAS</p>
            <h1>{view === 'seller' ? 'Pedidos y consultas' : view === 'inbox' ? 'Bandeja de entrada' : 'Pulso operativo'}</h1>
          </div>
          <div className="topbar__actions">
            <label className="seller-select">
              <Store size={16} />
              <select value={sellerId} onChange={event => { setSellerId(event.target.value); setInboxPage(0); setOperationsPage(0) }}>
                {sellers.map(seller => <option key={seller.id} value={seller.id}>{seller.name}</option>)}
              </select>
              <ChevronDown size={15} />
            </label>
            <button className="button button--ghost" onClick={resetDemo}><RefreshCcw size={16} />Reiniciar demo</button>
          </div>
        </header>

        <AppContent
          error={error}
          loading={loading}
          onRetry={() => { setError(''); void load() }}
          view={view}
          orders={orders}
          questions={questions}
          pending={pending}
          query={query}
          onQueryChange={setQuery}
          onOpenOrder={openOrder}
          orderStatus={orderStatus}
          orderFrom={orderFrom}
          orderTo={orderTo}
          onOrderStatusChange={value => { setOrderStatus(value); setInboxPage(0) }}
          onOrderFromChange={value => { setOrderFrom(value); setInboxPage(0) }}
          onOrderToChange={value => { setOrderTo(value); setInboxPage(0) }}
          groups={questionGroups}
          analyzed={groupsAnalyzed}
          analysis={analysis}
          inboxFilter={inboxFilter}
          inboxSearch={inboxSearch}
          onFilterChange={filter => { setInboxFilter(filter); setInboxPage(0) }}
          onSearchChange={search => { setInboxSearch(search); setInboxPage(0) }}
          questionTotal={questionTotal}
          visibleTotal={inboxVisibleTotal}
          pendingTotal={pendingTotal}
          inboxPage={inboxPage}
          inboxPageSize={inboxPageSize}
          inboxHasNext={inboxHasNext}
          onInboxPage={setInboxPage}
          onInboxPageSize={size => { setInboxPageSize(size); setInboxPage(0) }}
          onAnalyze={analyzeGroups}
          onExpandGroup={expandGroup}
          onAnswer={openAnswer}
          onAnswerGroup={group => openAnswer(group[0], group)}
          metrics={metrics}
          operationsPage={operationsPage}
          operationsPageSize={operationsPageSize}
          operationsHasNext={operationsHasNext}
          onOperationsPage={setOperationsPage}
          onOperationsPageSize={size => { setOperationsPageSize(size); setOperationsPage(0) }}
        />
      </main>
      {selectedOrder && <OrderDetailsModal order={selectedOrder} questions={orderQuestions} loadingQuestions={orderQuestionsLoading} onClose={closeOrder} onAnswer={openAnswer} onTransition={transitionOrder} onAsk={createOrderQuestion} />}
      {selected && <AnswerModal
        key={selected.id}
        question={selected}
        groupSize={selectedGroup.length}
        saving={saving}
        onClose={() => { setSelected(null); setSelectedGroup([]) }}
        onSubmit={submitAnswer}
      />}
    </div>
  )
}

export default App

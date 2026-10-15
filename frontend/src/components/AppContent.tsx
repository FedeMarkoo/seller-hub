import { AlertCircle } from 'lucide-react'
import { InboxView } from './InboxView'
import { OperationsView } from './OperationsView'
import { SellerView } from './SellerView'
import type { InboxFilter } from './InboxFilters'
import type { Metrics, Order, Question, QuestionGroup, QuestionGroupAnalysis, OrderStatus } from '../types'

interface Props {
  view: 'seller' | 'inbox' | 'operations'
  orders: Order[]
  questions: Question[]
  pending: Question[]
  query: string
  onQueryChange: (value: string) => void
  onOpenOrder: (order: Order) => void
  orderStatus?: OrderStatus
  orderFrom: string
  orderTo: string
  onOrderStatusChange: (value?: OrderStatus) => void
  onOrderFromChange: (value: string) => void
  onOrderToChange: (value: string) => void
  groups: QuestionGroup[]
  analyzed: boolean
  analysis: QuestionGroupAnalysis
  inboxFilter: InboxFilter
  inboxSearch: string
  onFilterChange: (filter: InboxFilter) => void
  onSearchChange: (value: string) => void
  questionTotal: number
  visibleTotal: number
  pendingTotal: number
  inboxPage: number
  inboxPageSize: number
  inboxHasNext: boolean
  onInboxPage: (page: number) => void
  onInboxPageSize: (size: number) => void
  onAnalyze: () => Promise<void>
  onExpandGroup: (id: string) => Promise<QuestionGroup>
  onAnswer: (question: Question, group?: Question[]) => void
  onAnswerGroup: (group: Question[]) => void
  metrics: Metrics | null
  operationsPage: number
  operationsPageSize: number
  operationsHasNext: boolean
  onOperationsPage: (page: number) => void
  onOperationsPageSize: (size: number) => void
}

export function AppContent({ error, loading, onRetry, ...props }: Props & { error: string; loading: boolean; onRetry: () => void }) {
  return (
    <div className="content">
      {error && <ErrorBanner message={error} onRetry={onRetry} />}
      {loading ? <LoadingState /> : error ? <UnavailableState /> : <CurrentView {...props} />}
    </div>
  )
}

function ErrorBanner({ message, onRetry }: { message: string; onRetry: () => void }) {
  return <div className="error" role="alert"><AlertCircle size={18} /><span>{message}</span><button onClick={onRetry}>Reintentar</button></div>
}

function LoadingState() {
  return <div className="loader"><span /><p>Actualizando información…</p></div>
}

function UnavailableState() {
  return <div className="loader"><p>No hay datos para mostrar hasta recuperar la conexión.</p></div>
}

function CurrentView(props: Props) {
  if (props.view === 'seller') {
    return (
      <SellerView
        orders={props.orders}
        questions={props.questions}
        pending={props.pending}
        query={props.query}
        onQueryChange={props.onQueryChange}
        onOpenOrder={props.onOpenOrder}
        status={props.orderStatus}
        from={props.orderFrom}
        to={props.orderTo}
        onStatusChange={props.onOrderStatusChange}
        onFromChange={props.onOrderFromChange}
        onToChange={props.onOrderToChange}
      />
    )
  }
  if (props.view === 'inbox') {
    return <InboxView
      groups={props.groups}
      analyzed={props.analyzed}
      analysis={props.analysis}
      filter={props.inboxFilter}
      search={props.inboxSearch}
      onFilterChange={props.onFilterChange}
      onSearchChange={props.onSearchChange}
      totalQuestions={props.questionTotal}
      visibleTotal={props.visibleTotal}
      pendingTotal={props.pendingTotal}
      page={props.inboxPage}
      pageSize={props.inboxPageSize}
      hasNext={props.inboxHasNext}
      onPage={props.onInboxPage}
      onPageSize={props.onInboxPageSize}
      onAnalyze={props.onAnalyze}
      onExpandGroup={props.onExpandGroup}
      onAnswer={props.onAnswer}
      onAnswerGroup={props.onAnswerGroup}
    />
  }
  return props.metrics ? <OperationsView
    metrics={props.metrics}
    questions={props.pending}
    orders={props.orders}
    total={props.questionTotal}
    page={props.operationsPage}
    pageSize={props.operationsPageSize}
    hasNext={props.operationsHasNext}
    onPage={props.onOperationsPage}
    onPageSize={props.onOperationsPageSize}
    onAnswer={question => props.onAnswer(question)}
  /> : null
}

import { Search } from 'lucide-react'
import type { QuestionGroupAnalysis, QuestionStatus } from '../types'

export type InboxFilter = QuestionStatus | 'ALL'

interface Props {
  filter: InboxFilter
  search: string
  analysis: QuestionGroupAnalysis
  analyzed: boolean
  totalQuestions: number
  pendingTotal: number
  onFilterChange: (filter: InboxFilter) => void
  onSearchChange: (search: string) => void
  onAnalyze: () => Promise<void>
}

export function InboxFilters({ filter, search, analysis, analyzed, totalQuestions, pendingTotal, onFilterChange, onSearchChange, onAnalyze }: Props) {
  return (
    <div className="inbox-tools">
      <button type="button" className="button button--primary" disabled={analysis.status === 'RUNNING'} onClick={() => void onAnalyze()}>
        {analysis.status === 'RUNNING' ? 'Analizando…' : analyzed ? 'Reanalizar grupos' : 'Analizar grupos'}
      </button>
      {analysis.status === 'RUNNING' && <span className="analysis-state">Proceso en curso</span>}
      {analysis.status === 'COMPLETED' && <span className="analysis-state">Grupos analizados</span>}
      {analysis.status === 'FAILED' && <span className="analysis-state analysis-state--error">Falló el análisis</span>}
      <div className="inbox-filters" role="group" aria-label="Filtrar preguntas">
        <button className={filter === 'PENDING' ? 'active' : ''} onClick={() => onFilterChange('PENDING')}>Pendientes <span>{pendingTotal}</span></button>
        <button className={filter === 'ALL' ? 'active' : ''} onClick={() => onFilterChange('ALL')}>Todas <span>{totalQuestions}</span></button>
        <button
          className={filter === 'ANSWERED' ? 'active' : ''}
          onClick={() => onFilterChange('ANSWERED')}
        >
          Respondidas <span>{totalQuestions - pendingTotal}</span>
        </button>
      </div>
      <label className="search inbox-search">
        <Search size={16} />
        <input value={search} onChange={event => onSearchChange(event.target.value)} placeholder="Buscar pregunta, pedido o comprador" />
      </label>
    </div>
  )
}

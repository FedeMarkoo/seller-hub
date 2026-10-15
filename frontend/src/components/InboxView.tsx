import { useState } from 'react'
import type { Question, QuestionGroup, QuestionGroupAnalysis } from '../types'
import { AnalysisEmptyState } from './AnalysisEmptyState'
import { InboxFilters, type InboxFilter } from './InboxFilters'
import { Pagination } from './Pagination'
import { QuestionGroupRow } from './QuestionGroupRow'

interface Props {
  groups: QuestionGroup[]
  analyzed: boolean
  analysis: QuestionGroupAnalysis
  filter: InboxFilter
  search: string
  totalQuestions: number
  visibleTotal: number
  pendingTotal: number
  page: number
  pageSize: number
  hasNext: boolean
  onFilterChange: (filter: InboxFilter) => void
  onSearchChange: (search: string) => void
  onPage: (page: number) => void
  onPageSize: (size: number) => void
  onAnalyze: () => Promise<void>
  onExpandGroup: (groupId: string) => Promise<QuestionGroup>
  onAnswer: (question: Question, group?: Question[]) => void
  onAnswerGroup: (group: Question[]) => void
}

export function InboxView({
  groups, analyzed, analysis, filter, search, totalQuestions, visibleTotal, pendingTotal,
  page, pageSize, hasNext, onFilterChange, onSearchChange, onPage, onPageSize,
  onAnalyze, onExpandGroup, onAnswer, onAnswerGroup
}: Props) {
  const [expandedGroups, setExpandedGroups] = useState<Set<string>>(new Set())
  const [loadingGroup, setLoadingGroup] = useState('')

  async function toggleGroup(group: QuestionGroup) {
    if (expandedGroups.has(group.id)) {
      setExpandedGroups(current => {
        const next = new Set(current)
        next.delete(group.id)
        return next
      })
      return
    }
    if (!group.questions.length) {
      setLoadingGroup(group.id)
      try {
        await onExpandGroup(group.id)
      } catch {
        return
      } finally {
        setLoadingGroup('')
      }
    }
    setExpandedGroups(current => new Set(current).add(group.id))
  }

  return (
    <>
      <section className="operations-intro">
        <div>
          <p className="eyebrow">ATENCIÓN AL COMPRADOR</p>
          <h2>Respondé a tiempo.</h2>
          <p>Priorizá las consultas pendientes y mantené cada conversación asociada a su pedido.</p>
        </div>
        <span className="live"><i /> {pendingTotal} PENDIENTES</span>
      </section>
      <section className="panel questions-panel inbox-panel">
        <div className="panel__header inbox-panel__header">
          <div><p className="eyebrow">CONVERSACIONES</p><h2>Bandeja de entrada</h2></div>
          <InboxFilters
            filter={filter}
            search={search}
            analysis={analysis}
            analyzed={analyzed}
            totalQuestions={totalQuestions}
            pendingTotal={pendingTotal}
            onFilterChange={onFilterChange}
            onSearchChange={onSearchChange}
            onAnalyze={onAnalyze}
          />
        </div>
        {!analyzed ? <AnalysisEmptyState onAnalyze={onAnalyze} /> : (
          <div className="question-list">
            {groups.length ? groups.map(group => (
              <QuestionGroupRow
                key={group.id}
                group={group}
                filter={filter}
                expanded={expandedGroups.has(group.id)}
                loading={loadingGroup === group.id}
                onToggle={toggleGroup}
                onAnswer={onAnswer}
                onAnswerGroup={onAnswerGroup}
                onExpand={groupToLoad => onExpandGroup(groupToLoad.id)}
              />
            )) : <p className="empty-state">No hay consultas que coincidan con el filtro.</p>}
          </div>
        )}
        <Pagination
          page={page}
          pageSize={pageSize}
          hasNext={hasNext}
          onPage={onPage}
          onPageSize={onPageSize}
          label={`Mostrando ${groups.length} grupos · ${visibleTotal} consultas`}
        />
      </section>
    </>
  )
}

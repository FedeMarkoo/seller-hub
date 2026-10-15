import { ChevronDown } from 'lucide-react'
import type { Question, QuestionGroup } from '../types'
import type { InboxFilter } from './InboxFilters'
import { QuestionItem } from './QuestionItem'

interface Props {
  group: QuestionGroup
  filter: InboxFilter
  expanded: boolean
  loading: boolean
  onToggle: (group: QuestionGroup) => void
  onAnswer: (question: Question, group: Question[]) => void
  onAnswerGroup: (group: Question[]) => void
  onExpand: (group: QuestionGroup) => Promise<QuestionGroup>
}

export function QuestionGroupRow({ group, filter, expanded, loading, onToggle, onAnswer, onAnswerGroup, onExpand }: Props) {
  const loadedQuestions = group.questions
  const pendingQuestions = loadedQuestions.filter(question => question.status === 'PENDING')
  const visibleCount = group.questionCount
  const canAnswer = filter !== 'ANSWERED' && group.pendingCount > 0
  const toggleLabel = loading
    ? 'Cargando…'
    : expanded
      ? 'Contraer'
      : loadedQuestions.length
        ? 'Expandir'
        : group.questionCount === 1
          ? 'Cargar consulta'
          : 'Cargar y expandir'

  async function answerGroup() {
    const loadedGroup = loadedQuestions.length ? group : await onExpand(group)
    const pending = loadedGroup.questions.filter(question => question.status === 'PENDING')
    if (pending.length) onAnswerGroup(pending)
  }

  return (
    <section className="question-group">
      <div
        className={`question-group__header${expanded ? ' is-expanded' : ''}`}
        role="button"
        tabIndex={0}
        aria-expanded={expanded}
        onClick={() => onToggle(group)}
        onKeyDown={event => {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault()
            onToggle(group)
          }
        }}
      >
        <div>
          <span className="eyebrow">{group.questionCount > 1 ? 'TEXTOS MUY SIMILARES' : 'CONSULTA'}</span>
          <h3>{group.representativeText}</h3>
        </div>
        <div className="question-group__actions">
          <span className="counter">{visibleCount} {visibleCount === 1 ? 'consulta' : 'consultas'}</span>
          {canAnswer && (
            <button
              type="button"
              className="button button--secondary"
              onMouseDown={event => event.stopPropagation()}
              onClick={event => {
                event.preventDefault()
                event.stopPropagation()
                void answerGroup()
              }}
            >
              {group.questionCount > 1 ? 'Responder todas' : 'Responder'}
            </button>
          )}
          <span className="group-toggle">
            {toggleLabel}
            <ChevronDown size={15} />
          </span>
        </div>
      </div>
      {expanded && loadedQuestions.map(question => (
        <QuestionItem
          key={question.id}
          question={question}
          group={pendingQuestions.length > 1 ? pendingQuestions : [question]}
          onAnswer={onAnswer}
        />
      ))}
    </section>
  )
}

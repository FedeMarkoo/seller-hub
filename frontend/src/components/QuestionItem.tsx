import { ArrowRight, CheckCircle2 } from 'lucide-react'
import type { Question } from '../types'

interface Props {
  question: Question
  group: Question[]
  onAnswer: (question: Question, group: Question[]) => void
}

export function QuestionItem({ question, group, onAnswer }: Props) {
  const initials = question.buyerName.split(' ').map(name => name[0]).slice(0, 2).join('')

  return (
    <article className={question.status === 'ANSWERED' ? 'question answered' : 'question'}>
      <div className="avatar">{initials}</div>
      <div className="question__body">
        <div><strong>{question.buyerName}</strong><span>{question.orderId} · hace {question.waitingHours} h</span></div>
        <p>{question.text}</p>
        {question.answer && <small className="answer-preview">Tu respuesta: {question.answer}</small>}
      </div>
      {question.status === 'PENDING' ? (
        <button type="button" className="button button--secondary" onClick={() => onAnswer(question, group)}>
          Responder <ArrowRight size={15} />
        </button>
      ) : <span className="answered-label"><CheckCircle2 size={16} />Respondida</span>}
    </article>
  )
}

import { useEffect, useRef, useState } from 'react'
import type { RefObject } from 'react'
import { MessageSquareText, Send, X } from 'lucide-react'
import type { Question } from '../types'

interface Props {
  question: Question | null
  groupSize?: number
  saving: boolean
  onClose: () => void
  onSubmit: (answer: string) => Promise<void>
}

export function AnswerModal({ question, groupSize = 1, saving, onClose, onSubmit }: Props) {
  const [answer, setAnswer] = useState('')
  const answerInput = useRef<HTMLTextAreaElement>(null)

  useEffect(() => {
    const previousFocus = document.activeElement as HTMLElement | null
    answerInput.current?.focus()
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      previousFocus?.focus()
    }
  }, [onClose])

  if (!question) return null

  const isBatch = groupSize > 1
  const submit = () => {
    if (answer.trim() && !saving) void onSubmit(answer)
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="answer-title"
        aria-describedby="question-text"
        onMouseDown={event => event.stopPropagation()}
      >
        <ModalCloseButton onClose={onClose} />
        <div className="modal__icon"><MessageSquareText size={22} /></div>
        <AnswerHeading question={question} isBatch={isBatch} groupSize={groupSize} />
        <AnswerComposer answer={answer} saving={saving} inputRef={answerInput} onChange={setAnswer} onSubmit={submit} />
        <AnswerFooter answerLength={answer.length} groupSize={groupSize} saving={saving} canSubmit={Boolean(answer.trim())} onSubmit={submit} />
      </section>
    </div>
  )
}

function ModalCloseButton({ onClose }: { onClose: () => void }) {
  return <button type="button" className="icon-button modal__close" onClick={onClose} aria-label="Cerrar"><X size={20} /></button>
}

function AnswerHeading({ question, isBatch, groupSize }: { question: Question; isBatch: boolean; groupSize: number }) {
  return (
    <>
      <p className="eyebrow">{isBatch ? 'RESPUESTA EN LOTE' : `${question.orderId} · ${question.buyerName}`}</p>
      <h2 id="answer-title">{isBatch ? `Responder ${groupSize} consultas similares` : 'Responder consulta'}</h2>
      <blockquote id="question-text">{isBatch ? 'Ejemplo de consulta del grupo: ' : ''}“{question.text}”</blockquote>
    </>
  )
}

interface AnswerComposerProps {
  answer: string
  saving: boolean
  inputRef: RefObject<HTMLTextAreaElement | null>
  onChange: (answer: string) => void
  onSubmit: () => void
}

function AnswerComposer({ answer, saving, inputRef, onChange, onSubmit }: AnswerComposerProps) {
  return (
    <>
      <label htmlFor="answer">Tu respuesta</label>
      <textarea
        ref={inputRef}
        id="answer"
        autoFocus
        maxLength={1000}
        rows={6}
        value={answer}
        onChange={event => onChange(event.target.value)}
        onKeyDown={event => {
          if ((event.ctrlKey || event.metaKey) && event.key === 'Enter' && answer.trim() && !saving) {
            event.preventDefault()
            onSubmit()
          }
        }}
        placeholder="Escribí una respuesta clara y útil para el comprador…"
      />
    </>
  )
}

interface AnswerFooterProps {
  answerLength: number
  groupSize: number
  saving: boolean
  canSubmit: boolean
  onSubmit: () => void
}

function AnswerFooter({ answerLength, groupSize, saving, canSubmit, onSubmit }: AnswerFooterProps) {
  const isBatch = groupSize > 1
  return (
    <div className="modal__footer">
      <small>{answerLength}/1000{isBatch ? ` · se aplicará a ${groupSize} consultas` : ''} · al enviar sigue la próxima pendiente</small>
      <button type="button" className="button button--primary" disabled={saving || !canSubmit} onClick={event => { event.preventDefault(); onSubmit() }}>
        {saving ? 'Enviando…' : isBatch ? 'Responder todas y seguir' : 'Enviar y seguir'} <Send size={16} />
      </button>
    </div>
  )
}

import { useState } from 'react'
import { MessageSquareText, Send, X } from 'lucide-react'
import type { Question } from '../types'

interface Props { question: Question | null; saving: boolean; onClose: () => void; onSubmit: (answer: string) => Promise<void> }

export function AnswerModal({ question, saving, onClose, onSubmit }: Props) {
  const [answer, setAnswer] = useState('')
  if (!question) return null
  return <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
    <section className="modal" role="dialog" aria-modal="true" aria-labelledby="answer-title" onMouseDown={e => e.stopPropagation()}>
      <button className="icon-button modal__close" onClick={onClose} aria-label="Cerrar"><X size={20} /></button>
      <div className="modal__icon"><MessageSquareText size={22} /></div>
      <p className="eyebrow">{question.orderId} · {question.buyerName}</p>
      <h2 id="answer-title">Responder consulta</h2>
      <blockquote>“{question.text}”</blockquote>
      <label htmlFor="answer">Tu respuesta</label>
      <textarea id="answer" autoFocus maxLength={1000} rows={6} value={answer} onChange={e => setAnswer(e.target.value)} placeholder="Escribí una respuesta clara y útil para el comprador…" />
      <div className="modal__footer"><small>{answer.length}/1000</small><button className="button button--primary" disabled={saving || !answer.trim()} onClick={() => onSubmit(answer)}>{saving ? 'Enviando…' : 'Enviar respuesta'} <Send size={16} /></button></div>
    </section>
  </div>
}

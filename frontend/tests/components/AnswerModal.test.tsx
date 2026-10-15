import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { AnswerModal } from '../../src/components/AnswerModal'

const question = {
  id: 'Q-1', orderId: 'ORD-1', sellerId: 'seller-01', buyerName: 'Ana Pérez',
  text: '¿Es compatible con Mac?', createdAt: '2026-10-01T00:00:00Z', status: 'PENDING' as const,
  priority: 'MEDIUM' as const, waitingHours: 3
}

describe('AnswerModal', () => {
  it('focuses the composer and submits with Ctrl+Enter', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<AnswerModal question={question} groupSize={2} saving={false} onClose={vi.fn()} onSubmit={onSubmit} />)
    const textarea = screen.getByRole('textbox')

    await waitFor(() => expect(document.activeElement).toBe(textarea))
    fireEvent.change(textarea, { target: { value: 'Sí, es compatible.' } })
    fireEvent.keyDown(textarea, { key: 'Enter', ctrlKey: true })

    expect(onSubmit).toHaveBeenCalledWith('Sí, es compatible.')
    expect(screen.getByRole('button', { name: /Responder todas y seguir/i })).toBeTruthy()
  })
})

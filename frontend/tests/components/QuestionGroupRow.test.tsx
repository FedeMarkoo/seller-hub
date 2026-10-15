import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { QuestionGroupRow } from '../../src/components/QuestionGroupRow'

const answeredGroup = {
  id: 'Q-1', representativeText: '¿Es compatible?', questionCount: 2, pendingCount: 0, questions: []
}

describe('QuestionGroupRow', () => {
  it('does not expose answer actions for answered groups', () => {
    render(
      <QuestionGroupRow
        group={answeredGroup}
        filter="ANSWERED"
        expanded={false}
        loading={false}
        onToggle={vi.fn()}
        onAnswer={vi.fn()}
        onAnswerGroup={vi.fn()}
        onExpand={vi.fn()}
      />,
    )

    expect(screen.queryByRole('button', { name: /Responder/i })).toBeNull()
    expect(screen.getByText('Cargar y expandir')).toBeTruthy()
  })
})

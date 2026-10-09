import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from './client'

describe('api client', () => {
  afterEach(() => vi.restoreAllMocks())

  it('sends an answer to the expected endpoint', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ id: 'Q-1', status: 'ANSWERED' }), { status: 200, headers: { 'Content-Type': 'application/json' } }))

    await api.answer('Q-1', 'Respuesta concreta')

    expect(fetchMock).toHaveBeenCalledWith('/api/questions/Q-1/answer', expect.objectContaining({
      method: 'POST', body: JSON.stringify({ answer: 'Respuesta concreta' })
    }))
  })
})

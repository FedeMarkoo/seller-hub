import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../../src/api/client'

describe('api client', () => {
  afterEach(() => vi.restoreAllMocks())

  it('sends an answer to the expected endpoint', async () => {
    const response = new Response(
      JSON.stringify({ id: 'Q-1', status: 'ANSWERED' }),
      { status: 200, headers: { 'Content-Type': 'application/json' } },
    )
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(response)

    await api.answer('demo-token', 'Q-1', 'Respuesta concreta')

    const [, options] = fetchMock.mock.calls[0]
    expect(options).toEqual(expect.objectContaining({ method: 'POST', body: JSON.stringify({ answer: 'Respuesta concreta' }) }))
    expect((options?.headers as Headers).get('X-Demo-Session')).toBe('demo-token')
  })

  it('sends inbox status and search to the backend', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ items: [] }), { status: 200 }))

    await api.questionGroups('demo-token', 'seller-01', 'PENDING', undefined, 2, 20, 'devolución')

    const [url] = fetchMock.mock.calls[0]
    expect(String(url)).toContain('sellerId=seller-01')
    expect(String(url)).toContain('status=PENDING')
    expect(String(url)).toContain('page=2')
    expect(String(url)).toContain('pageSize=20')
    expect(String(url)).toContain('search=devoluci%C3%B3n')
  })

  it('sends seller order search to the backend', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ items: [] }), { status: 200 }))

    await api.orders('demo-token', 'seller-01', undefined, 0, 'teclado')

    const [url] = fetchMock.mock.calls[0]
    expect(String(url)).toContain('sellerId=seller-01')
    expect(String(url)).toContain('search=teclado')
  })
})

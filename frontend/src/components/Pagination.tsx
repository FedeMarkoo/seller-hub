interface Props {
  page: number
  pageSize: number
  hasNext: boolean
  label: string
  onPage: (page: number) => void
  onPageSize: (size: number) => void
}

export function Pagination({ page, pageSize, hasNext, label, onPage, onPageSize }: Props) {
  return (
    <div className="pagination">
      <small>{label}</small>
      <div>
        <label>
          Por página
          <select value={pageSize} onChange={event => onPageSize(Number(event.target.value))}>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </label>
        <button className="button button--secondary" disabled={page === 0} onClick={() => onPage(page - 1)}>
          Anterior
        </button>
        <span>Página {page + 1}</span>
        <button className="button button--secondary" disabled={!hasNext} onClick={() => onPage(page + 1)}>
          Siguiente
        </button>
      </div>
    </div>
  )
}

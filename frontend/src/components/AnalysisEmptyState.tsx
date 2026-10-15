interface Props { onAnalyze: () => Promise<void> }

export function AnalysisEmptyState({ onAnalyze }: Props) {
  return (
    <div className="analysis-empty">
      <p>Los grupos todavía no fueron analizados.</p>
      <small>Ejecutá el análisis una vez para construir la bandeja agrupada.</small>
      <button type="button" className="button button--primary" onClick={() => void onAnalyze()}>Analizar grupos</button>
    </div>
  )
}

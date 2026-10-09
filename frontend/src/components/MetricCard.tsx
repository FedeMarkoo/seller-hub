import type { LucideIcon } from 'lucide-react'

interface Props { label: string; value: string; detail: string; icon: LucideIcon; tone?: 'ink' | 'orange' | 'green' }

export function MetricCard({ label, value, detail, icon: Icon, tone = 'ink' }: Props) {
  return <article className={`metric-card metric-card--${tone}`}>
    <div className="metric-card__top"><span>{label}</span><Icon size={19} /></div>
    <strong>{value}</strong><small>{detail}</small>
  </article>
}

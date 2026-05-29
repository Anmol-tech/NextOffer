type MetricProps = {
  label: string
  value: string
  detail: string
}

export function Metric({ label, value, detail }: MetricProps) {
  return (
    <article className="metric-card">
      <span>{label}</span>
      <strong>{value}</strong>
      <p>{detail}</p>
    </article>
  )
}

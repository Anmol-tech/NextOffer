type PanelHeaderProps = {
  title: string
  action?: string
  onAction?: () => void
}

export function PanelHeader({ title, action, onAction }: PanelHeaderProps) {
  return (
    <div className="panel-header">
      <h2>{title}</h2>
      {action && onAction && (
        <button onClick={onAction} type="button">
          {action}
        </button>
      )}
    </div>
  )
}

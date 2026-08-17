import './LoadingState.css';

interface LoadingStateProps {
  label?: string;
}

export function LoadingState({ label = 'Ładowanie…' }: LoadingStateProps) {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      <span className="loading-state__spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}

export function FullPageLoadingState({ label }: LoadingStateProps) {
  return (
    <div className="loading-state loading-state--full">
      <LoadingState label={label} />
    </div>
  );
}

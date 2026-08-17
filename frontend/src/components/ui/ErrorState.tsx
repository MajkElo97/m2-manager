import { Button } from './Button';
import './ErrorState.css';

interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
}

export function ErrorState({
  title = 'Wystąpił błąd',
  message,
  onRetry,
}: ErrorStateProps) {
  return (
    <div className="error-state" role="alert">
      <h2 className="error-state__title">{title}</h2>
      <p className="error-state__message">{message}</p>
      {onRetry ? (
        <Button variant="secondary" onClick={onRetry}>
          Spróbuj ponownie
        </Button>
      ) : null}
    </div>
  );
}

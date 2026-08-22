import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';

interface CredentialsDialogProps {
  isOpen: boolean;
  title: string;
  organizationName: string;
  login: string;
  temporaryPassword: string;
  onClose: () => void;
}

export function CredentialsDialog({
  isOpen,
  title,
  organizationName,
  login,
  temporaryPassword,
  onClose,
}: CredentialsDialogProps) {
  const copyCredentials = async () => {
    const text = [
      `Organizacja: ${organizationName}`,
      `Login: ${login}`,
      `Hasło tymczasowe: ${temporaryPassword}`,
    ].join('\n');

    await navigator.clipboard.writeText(text);
  };

  return (
    <Modal isOpen={isOpen} title={title} onClose={onClose} size="large">
      <p className="credentials-dialog__warning">
        Hasło tymczasowe zostanie pokazane tylko teraz. Przekaż je administratorowi organizacji.
      </p>
      <div className="credentials-dialog__grid">
        <div className="credentials-dialog__row">
          <span className="credentials-dialog__label">Organizacja</span>
          <strong className="credentials-dialog__value">{organizationName}</strong>
        </div>
        <div className="credentials-dialog__row">
          <span className="credentials-dialog__label">Login</span>
          <strong className="credentials-dialog__value">{login}</strong>
        </div>
        <div className="credentials-dialog__row">
          <span className="credentials-dialog__label">Hasło tymczasowe</span>
          <strong className="credentials-dialog__value">{temporaryPassword}</strong>
        </div>
      </div>
      <div className="credentials-dialog__actions">
        <Button type="button" variant="secondary" onClick={() => void copyCredentials()}>
          Kopiuj dane logowania
        </Button>
        <Button type="button" onClick={onClose}>
          Gotowe
        </Button>
      </div>
    </Modal>
  );
}

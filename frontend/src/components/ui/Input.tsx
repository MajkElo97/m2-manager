import type { InputHTMLAttributes } from 'react';
import './Input.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export function Input({ label, error, id, className = '', ...props }: InputProps) {
  const inputId = id ?? props.name;

  return (
    <label className={`ui-input ${error ? 'ui-input--error' : ''} ${className}`.trim()} htmlFor={inputId}>
      <span className="ui-input__label">{label}</span>
      <input id={inputId} className="ui-input__control" {...props} />
      {error ? <span className="ui-input__error">{error}</span> : null}
    </label>
  );
}

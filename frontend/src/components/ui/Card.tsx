import type { ReactNode } from 'react';
import './Card.css';

interface CardProps {
  title?: string;
  children: ReactNode;
  className?: string;
}

export function Card({ title, children, className = '' }: CardProps) {
  return (
    <section className={`ui-card ${className}`.trim()}>
      {title ? <h3 className="ui-card__title">{title}</h3> : null}
      <div className="ui-card__body">{children}</div>
    </section>
  );
}

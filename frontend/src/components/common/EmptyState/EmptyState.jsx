import { Link } from 'react-router-dom';
import styles from './EmptyState.module.css';
import { Button } from '../Button/Button';

export function EmptyState({ title, description, actionLabel, onAction, actionTo }) {
  return (
    <div className={styles.empty}>
      <div className={styles.empty__icon} aria-hidden="true" />
      <h3 className={styles.empty__title}>{title}</h3>
      {description ? <p className={styles.empty__desc}>{description}</p> : null}
      {actionLabel && actionTo ? (
        <Link to={actionTo}>
          <Button variant="primary">{actionLabel}</Button>
        </Link>
      ) : actionLabel && onAction ? (
        <Button variant="primary" onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}

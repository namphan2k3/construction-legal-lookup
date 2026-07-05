import styles from './Badge.module.css';

export function Badge({ children, variant = 'neutral', className = '' }) {
  return (
    <span className={`${styles.badge} ${styles[`badge--${variant}`]} ${className}`}>
      {children}
    </span>
  );
}

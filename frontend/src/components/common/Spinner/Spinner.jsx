import styles from './Spinner.module.css';

export function Spinner({ size = 'md', className = '' }) {
  return (
    <div
      className={`${styles.spinner} ${styles[`spinner--${size}`]} ${className}`}
      role="status"
      aria-label="Đang tải"
    />
  );
}

export function LoadingOverlay({ message = 'Đang tải...' }) {
  return (
    <div className={styles.overlay}>
      <Spinner size="lg" />
      <p className={styles.overlay__text}>{message}</p>
    </div>
  );
}

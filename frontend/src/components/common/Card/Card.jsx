import styles from './Card.module.css';

export function Card({ children, className = '', padding = 'md', hover = false, ...props }) {
  return (
    <div
      className={`${styles.card} ${styles[`card--${padding}`]} ${hover ? styles['card--hover'] : ''} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}

export function CardGrid({ children, columns = 3, className = '' }) {
  return (
    <div className={`${styles.grid} ${styles[`grid--${columns}`]} ${className}`}>
      {children}
    </div>
  );
}

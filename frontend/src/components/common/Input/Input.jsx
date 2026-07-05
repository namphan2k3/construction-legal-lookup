import styles from './Input.module.css';

export function Input({
  label,
  error,
  id,
  className = '',
  ...props
}) {
  const inputId = id || props.name;

  return (
    <div className={`${styles.field} ${className}`}>
      {label ? (
        <label htmlFor={inputId} className={styles.field__label}>
          {label}
        </label>
      ) : null}
      <input
        id={inputId}
        className={`${styles.field__input} ${error ? styles['field__input--error'] : ''}`}
        {...props}
      />
      {error ? <span className={styles.field__error}>{error}</span> : null}
    </div>
  );
}

export function Select({ label, error, id, children, className = '', ...props }) {
  const selectId = id || props.name;

  return (
    <div className={`${styles.field} ${className}`}>
      {label ? (
        <label htmlFor={selectId} className={styles.field__label}>
          {label}
        </label>
      ) : null}
      <select
        id={selectId}
        className={`${styles.field__input} ${styles.field__select} ${error ? styles['field__input--error'] : ''}`}
        {...props}
      >
        {children}
      </select>
      {error ? <span className={styles.field__error}>{error}</span> : null}
    </div>
  );
}

export function Textarea({ label, error, id, className = '', ...props }) {
  const textareaId = id || props.name;

  return (
    <div className={`${styles.field} ${className}`}>
      {label ? (
        <label htmlFor={textareaId} className={styles.field__label}>
          {label}
        </label>
      ) : null}
      <textarea
        id={textareaId}
        className={`${styles.field__input} ${styles.field__textarea} ${error ? styles['field__input--error'] : ''}`}
        {...props}
      />
      {error ? <span className={styles.field__error}>{error}</span> : null}
    </div>
  );
}

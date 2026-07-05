import styles from './Button.module.css';

const VARIANTS = ['primary', 'secondary', 'ghost', 'danger', 'accent'];
const SIZES = ['sm', 'md', 'lg'];

export function Button({
  children,
  variant = 'primary',
  size = 'md',
  type = 'button',
  disabled = false,
  loading = false,
  fullWidth = false,
  className = '',
  ...props
}) {
  const variantClass = VARIANTS.includes(variant) ? styles[`button--${variant}`] : styles['button--primary'];
  const sizeClass = SIZES.includes(size) ? styles[`button--${size}`] : styles['button--md'];

  const spinnerClass =
    variant === 'secondary' || variant === 'ghost'
      ? styles['button__spinner--dark']
      : styles['button__spinner--light'];

  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`${styles.button} ${variantClass} ${sizeClass} ${fullWidth ? styles['button--full'] : ''} ${loading ? styles['button--loading'] : ''} ${className}`}
      {...props}
    >
      {loading ? <span className={`${styles.button__spinner} ${spinnerClass}`} aria-hidden="true" /> : null}
      <span className={loading ? styles.button__textHidden : undefined}>{children}</span>
    </button>
  );
}

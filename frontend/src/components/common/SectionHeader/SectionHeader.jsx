import styles from './SectionHeader.module.css';
import { Link } from 'react-router-dom';

export function SectionHeader({ title, subtitle, actionLabel, actionTo }) {
  return (
    <div className={styles.header}>
      <div>
        <h2 className={styles.header__title}>{title}</h2>
        {subtitle ? <p className={styles.header__subtitle}>{subtitle}</p> : null}
      </div>
      {actionLabel && actionTo ? (
        <Link to={actionTo} className={styles.header__action}>
          {actionLabel}
        </Link>
      ) : null}
    </div>
  );
}

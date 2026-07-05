import styles from './Pagination.module.css';
import { Button } from '../Button/Button';

export function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const canPrev = page > 0;
  const canNext = page < totalPages - 1;

  return (
    <nav className={styles.pagination} aria-label="Phân trang">
      <Button
        variant="secondary"
        size="sm"
        disabled={!canPrev}
        onClick={() => onPageChange(page - 1)}
      >
        Trước
      </Button>

      <span className={styles.pagination__info}>
        <span className={styles.pagination__current}>{page + 1}</span>
        <span className={styles.pagination__sep}>/</span>
        <span>{totalPages}</span>
      </span>

      <Button
        variant="secondary"
        size="sm"
        disabled={!canNext}
        onClick={() => onPageChange(page + 1)}
      >
        Sau
      </Button>
    </nav>
  );
}

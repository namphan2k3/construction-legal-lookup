import { Link } from 'react-router-dom';
import { Badge } from '@/components/common/Badge/Badge';
import {
  formatDate,
  getDocumentTypeLabel,
  getStatusLabel,
  getStatusVariant,
  truncate,
} from '@/utils/formatters';
import styles from './DocumentCard.module.css';

export function DocumentCard({ document, showViews = false, compact = false }) {
  const statusVariant = getStatusVariant(document.status);

  return (
    <article className={`${styles.card} ${compact ? styles['card--compact'] : ''}`}>
      <Link to={`/documents/${document.id}`} className={styles.card__link}>
        <div className={styles.card__top}>
          <Badge variant="primary">{getDocumentTypeLabel(document.documentType)}</Badge>
          <Badge variant={statusVariant}>{getStatusLabel(document.status)}</Badge>
        </div>

        <p className={styles.card__number}>{document.documentNumber}</p>
        <h3 className={styles.card__title}>{document.title}</h3>

        {!compact && document.abstract ? (
          <p className={styles.card__abstract}>{truncate(document.abstract, 140)}</p>
        ) : null}

        <div className={styles.card__meta}>
          {document.issuingBody ? <span>{document.issuingBody}</span> : null}
          {document.issuedDate ? (
            <>
              <span className={styles.card__dot} aria-hidden="true">·</span>
              <span>{formatDate(document.issuedDate)}</span>
            </>
          ) : null}
          {showViews && document.viewCount != null ? (
            <>
              <span className={styles.card__dot} aria-hidden="true">·</span>
              <span>{document.viewCount} lượt xem</span>
            </>
          ) : null}
        </div>
      </Link>
    </article>
  );
}

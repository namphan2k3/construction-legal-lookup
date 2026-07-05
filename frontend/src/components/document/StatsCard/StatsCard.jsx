import styles from './StatsCard.module.css';
import { formatNumber } from '@/utils/formatters';

export function StatsCard({ label, value, suffix = '' }) {
  return (
    <div className={styles.stat}>
      <p className={styles.stat__value}>
        {formatNumber(value)}{suffix}
      </p>
      <p className={styles.stat__label}>{label}</p>
    </div>
  );
}

export function StatsGrid({ stats }) {
  if (!stats) return null;

  return (
    <div className={styles.grid}>
      <StatsCard label="Văn bản" value={stats.totalDocuments} />
      <StatsCard label="Loại văn bản" value={stats.totalTypes} />
      <StatsCard label="Cơ quan ban hành" value={stats.totalIssuingBodies} />
      <div className={styles.stat}>
        <p className={styles.stat__valueSmall}>{stats.datasetLabel || '—'}</p>
        <p className={styles.stat__label}>Cập nhật dữ liệu</p>
      </div>
    </div>
  );
}

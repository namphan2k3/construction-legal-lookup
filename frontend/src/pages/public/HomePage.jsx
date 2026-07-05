import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { StatsGrid } from '@/components/document/StatsCard/StatsCard';
import { DocumentCard } from '@/components/document/DocumentCard/DocumentCard';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Badge } from '@/components/common/Badge/Badge';
import {
  getStats,
  getRecentDocuments,
  getPopularDocuments,
  getUpdatedDocuments,
  getCategories,
} from '@/services/public.service';
import styles from './HomePage.module.css';

export default function HomePage() {
  const [stats, setStats] = useState(null);
  const [recentDocs, setRecentDocs] = useState([]);
  const [popularDocs, setPopularDocs] = useState([]);
  const [updatedDocs, setUpdatedDocs] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      getStats(),
      getRecentDocuments(6),
      getPopularDocuments(6),
      getUpdatedDocuments(6),
      getCategories(),
    ])
      .then(([statsData, recent, popular, updated, cats]) => {
        setStats(statsData);
        setRecentDocs(recent);
        setPopularDocs(popular);
        setUpdatedDocs(updated);
        setCategories(cats);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="page">
        <div className="container">
          <LoadingOverlay message="Đang tải dữ liệu..." />
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <section className={styles.hero}>
        <div className={`container ${styles.hero__inner}`}>
          <div className={styles.hero__content}>
            <h1 className={styles.hero__title}>
              Thư viện pháp luật ngành xây dựng
            </h1>
            <p className={styles.hero__desc}>
              Kho văn bản pháp luật hiện hành, được cập nhật thường xuyên. Tra cứu nhanh chóng, hiểu rõ quy định.
            </p>
            <Link to="/search" className={styles.hero__button}>
              Bắt đầu tra cứu
            </Link>
          </div>
        </div>
      </section>

      <div className="container">
        <section className={styles.section}>
          <StatsGrid stats={stats} />
        </section>

        {recentDocs.length > 0 && (
          <section className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>Văn bản mới ban hành</h2>
              <Link to="/search?sort=issuedDate,desc" className={styles.sectionLink}>
                Xem tất cả →
              </Link>
            </div>
            <div className={styles.documentGrid}>
              {recentDocs.map((doc) => (
                <DocumentCard key={doc.id} document={doc} />
              ))}
            </div>
          </section>
        )}

        {popularDocs.length > 0 && (
          <section className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>Văn bản nổi bật</h2>
              <Link to="/search?sort=viewCount,desc" className={styles.sectionLink}>
                Xem tất cả →
              </Link>
            </div>
            <div className={styles.documentGrid}>
              {popularDocs.map((doc) => (
                <DocumentCard key={doc.id} document={doc} showViews />
              ))}
            </div>
          </section>
        )}

        {updatedDocs.length > 0 && (
          <section className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>Văn bản mới cập nhật</h2>
              <Link to="/search?sort=updatedAt,desc" className={styles.sectionLink}>
                Xem tất cả →
              </Link>
            </div>
            <div className={styles.documentGrid}>
              {updatedDocs.map((doc) => (
                <DocumentCard key={doc.id} document={doc} />
              ))}
            </div>
          </section>
        )}

        {categories.length > 0 && (
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>Danh mục</h2>
            <div className={styles.categoryGrid}>
              {categories.map((cat) => (
                <Link
                  key={cat.id}
                  to={`/search?categoryId=${cat.id}`}
                  className={styles.categoryCard}
                >
                  <h3 className={styles.categoryCard__title}>{cat.name}</h3>
                  <Badge variant="secondary">{cat.documentCount} văn bản</Badge>
                </Link>
              ))}
            </div>
          </section>
        )}
      </div>
    </div>
  );
}

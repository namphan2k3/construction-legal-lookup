import { useState, useEffect } from 'react';
import { Card } from '@/components/common/Card/Card';
import { Button } from '@/components/common/Button/Button';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Badge } from '@/components/common/Badge/Badge';
import { getAdminStats } from '@/services/admin.service';
import { formatDate } from '@/utils/formatters';
import styles from './AdminDashboardPage.module.css';

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [period, setPeriod] = useState('7d');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAdminStats(period)
      .then((statsData) => {
        setStats(statsData);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [period]);

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
      <div className="container">
        <header className="page__header">
          <div className={styles.header__top}>
            <div>
              <h1 className="page__title">Quản trị hệ thống</h1>
              <p className="page__subtitle">
                Tổng quan về hoạt động và thống kê hệ thống.
              </p>
            </div>
            <div className={styles.periodSelector}>
              <Button
                variant={period === '7d' ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => setPeriod('7d')}
              >
                7 ngày
              </Button>
              <Button
                variant={period === '30d' ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => setPeriod('30d')}
              >
                30 ngày
              </Button>
              <Button
                variant={period === 'all' ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => setPeriod('all')}
              >
                Tất cả
              </Button>
            </div>
          </div>
        </header>

        {stats && (
          <>
            <section className={styles.stats}>
              <div className={styles.statCard}>
                <h3 className={styles.statCard__label}>Tổng văn bản</h3>
                <p className={styles.statCard__value}>{stats.overview?.totalDocuments || 0}</p>
              </div>
              <div className={styles.statCard}>
                <h3 className={styles.statCard__label}>Người dùng</h3>
                <p className={styles.statCard__value}>{stats.overview?.totalUsers || 0}</p>
              </div>
              <div className={styles.statCard}>
                <h3 className={styles.statCard__label}>Lượt tìm kiếm</h3>
                <p className={styles.statCard__value}>{stats.overview?.totalSearches || 0}</p>
              </div>
              <div className={styles.statCard}>
                <h3 className={styles.statCard__label}>Lượt xem</h3>
                <p className={styles.statCard__value}>{stats.overview?.totalViews || 0}</p>
              </div>
              <div className={styles.statCard}>
                <h3 className={styles.statCard__label}>Lượt tải</h3>
                <p className={styles.statCard__value}>{stats.overview?.totalDownloads || 0}</p>
              </div>
              <div className={styles.statCard}>
                <h3 className={styles.statCard__label}>Yêu cầu AI</h3>
                <p className={styles.statCard__value}>{stats.overview?.totalAiRequests || 0}</p>
              </div>
            </section>

            <div className={styles.grid}>
              {stats.topKeywords && stats.topKeywords.length > 0 && (
                <section className={styles.section}>
                  <h2 className={styles.section__title}>Từ khóa phổ biến</h2>
                  <Card padding="md">
                    <div className={styles.keywordList}>
                      {stats.topKeywords.map((item, index) => (
                        <div key={index} className={styles.keywordItem}>
                          <span className={styles.keywordItem__rank}>#{index + 1}</span>
                          <span className={styles.keywordItem__keyword}>{item.keyword}</span>
                          <Badge variant="secondary">{item.count} lần</Badge>
                        </div>
                      ))}
                    </div>
                  </Card>
                </section>
              )}

              {stats.topDocuments && stats.topDocuments.length > 0 && (
                <section className={styles.section}>
                  <h2 className={styles.section__title}>Văn bản xem nhiều</h2>
                  <Card padding="md">
                    <div className={styles.documentList}>
                      {stats.topDocuments.map((doc) => (
                        <div key={doc.id} className={styles.documentItem}>
                          <div className={styles.documentItem__main}>
                            <p className={styles.documentItem__number}>{doc.documentNumber}</p>
                            <p className={styles.documentItem__title}>{doc.title}</p>
                          </div>
                          <Badge variant="primary">{doc.viewCount} lượt xem</Badge>
                        </div>
                      ))}
                    </div>
                  </Card>
                </section>
              )}
            </div>

            {stats.recentActivity && stats.recentActivity.length > 0 && (
              <section className={styles.section}>
                <h2 className={styles.section__title}>Hoạt động gần đây</h2>
                <Card padding="md">
                  <div className={styles.activityList}>
                    {stats.recentActivity.map((activity, index) => (
                      <div key={index} className={styles.activityItem}>
                        <Badge variant={activity.eventType === 'SEARCH' ? 'primary' : 'secondary'}>
                          {activity.eventType}
                        </Badge>
                        <span className={styles.activityItem__user}>
                          {activity.user?.fullName || activity.user?.email || 'Người dùng ẩn danh'}
                        </span>
                        <span className={styles.activityItem__detail}>
                          {activity.eventType === 'SEARCH' ? `Tìm kiếm: "${activity.metadata?.query}"` : ''}
                        </span>
                        <span className={styles.activityItem__time}>
                          {formatDate(activity.createdAt)}
                        </span>
                      </div>
                    ))}
                  </div>
                </Card>
              </section>
            )}
          </>
        )}
      </div>
    </div>
  );
}

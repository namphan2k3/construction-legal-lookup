import { useState, useEffect } from 'react';
import { Card } from '@/components/common/Card/Card';
import { Button } from '@/components/common/Button/Button';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Badge } from '@/components/common/Badge/Badge';
import { triggerCrawl, getCrawlLogs } from '@/services/admin.service';
import { formatDate } from '@/utils/formatters';
import styles from './AdminCrawlPage.module.css';

const STATUS_VARIANTS = {
  RUNNING: 'primary',
  COMPLETED: 'success',
  FAILED: 'danger',
  PENDING: 'secondary',
};

export default function AdminCrawlPage() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [mode, setMode] = useState('incremental');
  const [source, setSource] = useState('all');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadLogs();
  }, [page]);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const params = { page, size: 20 };
      const data = await getCrawlLogs(params);
      setLogs(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error('Failed to load crawl logs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSync = async () => {
    if (!confirm(`Bạn có chắc muốn chạy đồng bộ ${mode} từ ${source}?`)) return;
    setSyncing(true);
    try {
      await triggerCrawl(mode, source);
      alert('Đã kích hoạt đồng bộ dữ liệu');
      loadLogs();
    } catch (error) {
      console.error('Failed to trigger crawl:', error);
      alert('Không thể kích hoạt đồng bộ');
    } finally {
      setSyncing(false);
    }
  };

  const getStatusLabel = (status) => {
    const labels = {
      RUNNING: 'Đang chạy',
      COMPLETED: 'Hoàn thành',
      FAILED: 'Thất bại',
      PENDING: 'Chờ xử lý',
    };
    return labels[status] || status;
  };

  const getStatusVariant = (status) => {
    return STATUS_VARIANTS[status] || 'secondary';
  };

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <h1 className="page__title">Crawl & Đồng bộ dữ liệu</h1>
          <p className="page__subtitle">
            Quản lý và theo dõi quá trình đồng bộ dữ liệu từ các nguồn.
          </p>
        </header>

        <Card padding="md">
          <div className={styles.syncSection}>
            <h3 className={styles.sectionTitle}>Kích hoạt đồng bộ</h3>
            <div className={styles.syncForm}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Chế độ đồng bộ:</label>
                <select
                  value={mode}
                  onChange={(e) => setMode(e.target.value)}
                  className={styles.select}
                >
                  <option value="incremental">Tăng dần (incremental)</option>
                  <option value="full">Đầy đủ (full)</option>
                </select>
              </div>
              <div className={styles.formGroup}>
                <label className={styles.label}>Nguồn dữ liệu:</label>
                <select
                  value={source}
                  onChange={(e) => setSource(e.target.value)}
                  className={styles.select}
                >
                  <option value="all">Tất cả nguồn</option>
                  <option value="vbqppl">Văn bản quy phạm pháp luật</option>
                  <option value="thuvienphapluat">Thư viện pháp luật</option>
                </select>
              </div>
              <Button
                variant="primary"
                onClick={handleSync}
                disabled={syncing}
              >
                {syncing ? 'Đang đồng bộ...' : 'Bắt đầu đồng bộ'}
              </Button>
            </div>
          </div>
        </Card>

        <Card padding="md">
          <h3 className={styles.sectionTitle}>Nhật ký đồng bộ</h3>
          {loading ? (
            <LoadingOverlay message="Đang tải dữ liệu..." />
          ) : (
            <>
              {logs.length > 0 ? (
                <div className={styles.list}>
                  {logs.map((log) => (
                    <div key={log.id} className={styles.item}>
                      <div className={styles.item__main}>
                        <div className={styles.item__header}>
                          <Badge variant={getStatusVariant(log.status)}>
                            {getStatusLabel(log.status)}
                          </Badge>
                          <span className={styles.item__id}>#{log.id}</span>
                        </div>
                        <div className={styles.item__details}>
                          <div className={styles.detail}>
                            <span className={styles.detailLabel}>Chế độ:</span>
                            <span>{log.mode}</span>
                          </div>
                          <div className={styles.detail}>
                            <span className={styles.detailLabel}>Nguồn:</span>
                            <span>{log.source}</span>
                          </div>
                          {log.documentsProcessed !== undefined && (
                            <div className={styles.detail}>
                              <span className={styles.detailLabel}>Văn bản:</span>
                              <span>{log.documentsProcessed}</span>
                            </div>
                          )}
                          {log.errorMessage && (
                            <div className={styles.detail}>
                              <span className={styles.detailLabel}>Lỗi:</span>
                              <span className={styles.error}>{log.errorMessage}</span>
                            </div>
                          )}
                        </div>
                        <div className={styles.item__times}>
                          <div className={styles.time}>
                            <span className={styles.timeLabel}>Bắt đầu:</span>
                            <span>{formatDate(log.startedAt)}</span>
                          </div>
                          {log.completedAt && (
                            <div className={styles.time}>
                              <span className={styles.timeLabel}>Hoàn thành:</span>
                              <span>{formatDate(log.completedAt)}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className={styles.emptyText}>Chưa có nhật ký đồng bộ nào.</p>
              )}

              {totalPages > 1 && (
                <div className={styles.pagination}>
                  <Button
                    variant="secondary"
                    size="sm"
                    disabled={page === 0}
                    onClick={() => setPage(page - 1)}
                  >
                    Trang trước
                  </Button>
                  <span className={styles.pageInfo}>
                    Trang {page + 1} / {totalPages}
                  </span>
                  <Button
                    variant="secondary"
                    size="sm"
                    disabled={page >= totalPages - 1}
                    onClick={() => setPage(page + 1)}
                  >
                    Trang sau
                  </Button>
                </div>
              )}
            </>
          )}
        </Card>
      </div>
    </div>
  );
}

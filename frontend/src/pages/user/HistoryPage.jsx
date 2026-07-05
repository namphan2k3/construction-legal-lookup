import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { DocumentCard } from '@/components/document/DocumentCard/DocumentCard';
import { CardGrid } from '@/components/common/Card/Card';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Button } from '@/components/common/Button/Button';
import { EmptyState } from '@/components/common/EmptyState/EmptyState';
import { getSearchHistory, clearSearchHistory } from '@/services/history.service';
import { formatDate } from '@/utils/formatters';
import styles from './HistoryPage.module.css';

export default function HistoryPage({ tab = 'search' }) {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const fetchHistory = async () => {
      try {
        const data = await getSearchHistory();
        setItems(data || []);
      } catch (err) {
        console.error(err);
        setItems([]);
      } finally {
        setLoading(false);
      }
    };
    fetchHistory();
  }, []);

  const handleClear = async () => {
    try {
      await clearSearchHistory();
      setItems([]);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSearchAgain = (query) => {
    navigate(`/search?q=${encodeURIComponent(query)}`);
  };

  if (loading) {
    return (
      <div className="page">
        <div className="container">
          <LoadingOverlay message="Đang tải lịch sử..." />
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <h1 className="page__title">Lịch sử tìm kiếm</h1>
          <p className="page__subtitle">
            Xem lại các từ khóa và bộ lọc bạn đã tìm kiếm.
          </p>
        </header>

        {items.length > 0 && (
          <div className={styles.actions}>
            <Button variant="secondary" onClick={handleClear}>
              Xóa tất cả
            </Button>
          </div>
        )}

        {loading ? (
          <LoadingOverlay message="Đang tải..." />
        ) : items.length > 0 ? (
          <div className={styles.searchList}>
            {items.map((item, index) => (
              <div
                key={index}
                className={styles.searchItem}
                onClick={() => handleSearchAgain(item.query)}
              >
                <div className={styles.searchItem__query}>
                  <strong>{item.query || '(Không có từ khóa)'}</strong>
                </div>
                <div className={styles.searchItem__meta}>
                  <span className={styles.searchItem__date}>
                    {formatDate(item.createdAt)}
                  </span>
                  {item.resultCount != null && (
                    <span className={styles.searchItem__count}>
                      {item.resultCount} kết quả
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState
            title="Chưa có lịch sử"
            description="Bạn chưa thực hiện tìm kiếm nào."
          />
        )}
      </div>
    </div>
  );
}

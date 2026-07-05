import { Link } from 'react-router-dom';
import { Button } from '@/components/common/Button/Button';
import styles from './NotFoundPage.module.css';

export default function NotFoundPage() {
  return (
    <div className="page">
      <div className="container">
        <div className={styles.notFound}>
          <h1 className={styles.notFound__code}>404</h1>
          <h2 className={styles.notFound__title}>Trang không tìm thấy</h2>
          <p className={styles.notFound__desc}>
            Trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.
          </p>
          <div className={styles.notFound__actions}>
            <Button variant="primary" onClick={() => window.history.back()}>
              Quay lại
            </Button>
            <Link to="/">
              <Button variant="secondary">Về trang chủ</Button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

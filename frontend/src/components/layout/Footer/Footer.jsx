import { Link } from 'react-router-dom';
import { useAuthStore, selectIsAuthenticated } from '@/stores/authStore';
import styles from './Footer.module.css';

const AUTH_LINKS = [
  { to: '/bookmarks', label: 'Yêu thích' },
  { to: '/history/search', label: 'Lịch sử' },
  { to: '/profile', label: 'Hồ sơ' },
];

export function Footer() {
  const isAuthenticated = useAuthStore(selectIsAuthenticated);

  return (
    <footer className={styles.footer}>
      <div className={`container ${styles.footer__inner}`}>
        <div className={styles.footer__brand}>
          <p className={styles.footer__title}>Tra cứu Văn bản Pháp lý Xây dựng</p>
          <p className={styles.footer__desc}>
            Hệ thống tra cứu văn bản pháp luật ngành xây dựng hiện hành — tìm kiếm nhanh, đọc trực tuyến, hỗ trợ AI.
          </p>
        </div>
        <div className={styles.footer__links}>
          <Link to="/search">Tra cứu văn bản</Link>
          {isAuthenticated ? (
            AUTH_LINKS.map((link) => (
              <Link key={link.to} to={link.to}>
                {link.label}
              </Link>
            ))
          ) : (
            <>
              <Link to="/login">Đăng nhập</Link>
              <Link to="/register">Đăng ký</Link>
            </>
          )}
        </div>
        <p className={styles.footer__copy}>
          © {new Date().getFullYear()} Construction Legal Lookup
        </p>
      </div>
    </footer>
  );
}

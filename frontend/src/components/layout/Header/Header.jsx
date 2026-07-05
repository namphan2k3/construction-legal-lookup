import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore, selectIsAuthenticated, selectIsAdmin } from '@/stores/authStore';
import { useUIStore } from '@/stores/uiStore';
import { logout } from '@/services/auth.service';
import { Button } from '@/components/common/Button/Button';
import styles from './Header.module.css';

const NAV_LINKS = [
  { to: '/', label: 'Trang chủ', end: true },
  { to: '/search', label: 'Tra cứu' },
];

const USER_LINKS = [
  { to: '/bookmarks', label: 'Yêu thích' },
  { to: '/history/search', label: 'Lịch sử' },
  { to: '/profile', label: 'Hồ sơ' },
];

export function Header() {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore(selectIsAuthenticated);
  const isAdmin = useAuthStore(selectIsAdmin);
  const user = useAuthStore((s) => s.user);
  const mobileMenuOpen = useUIStore((s) => s.mobileMenuOpen);
  const toggleMobileMenu = useUIStore((s) => s.toggleMobileMenu);
  const closeMobileMenu = useUIStore((s) => s.closeMobileMenu);

  const handleLogout = async () => {
    closeMobileMenu();
    await logout();
    navigate('/');
  };

  return (
    <header className={styles.header}>
      <div className={`container ${styles.header__inner}`}>
        <Link to="/" className={styles.header__brand} onClick={closeMobileMenu}>
          <span className={styles.header__brandMark}>CLL</span>
          <span className={styles.header__brandText}>
            <strong>Tra cứu Pháp lý</strong>
            <small>Ngành Xây dựng</small>
          </span>
        </Link>

        <nav className={`${styles.header__nav} ${mobileMenuOpen ? styles['header__nav--open'] : ''}`}>
          {NAV_LINKS.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                `${styles.header__link} ${isActive ? styles['header__link--active'] : ''}`
              }
              onClick={closeMobileMenu}
            >
              {link.label}
            </NavLink>
          ))}

          {isAuthenticated ? (
            <>
              {USER_LINKS.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `${styles.header__link} ${isActive ? styles['header__link--active'] : ''}`
                  }
                  onClick={closeMobileMenu}
                >
                  {link.label}
                </NavLink>
              ))}
              {isAdmin ? (
                <NavLink
                  to="/admin"
                  className={({ isActive }) =>
                    `${styles.header__link} ${styles.header__linkAdmin} ${isActive ? styles['header__link--active'] : ''}`
                  }
                  onClick={closeMobileMenu}
                >
                  Quản trị
                </NavLink>
              ) : null}
            </>
          ) : null}
        </nav>

        <div className={styles.header__actions}>
          {isAuthenticated ? (
            <div className={styles.header__user}>
              <span className={styles.header__userName}>{user?.fullName || user?.email}</span>
              <Button variant="ghost" size="sm" onClick={handleLogout}>
                Đăng xuất
              </Button>
            </div>
          ) : (
            <>
              <Link to="/login" className={styles.header__loginLink}>
                Đăng nhập
              </Link>
              <Button variant="primary" size="sm" onClick={() => navigate('/register')}>
                Đăng ký
              </Button>
            </>
          )}

          <button
            type="button"
            className={`${styles.header__menuBtn} ${mobileMenuOpen ? styles['header__menuBtn--open'] : ''}`}
            onClick={toggleMobileMenu}
            aria-label={mobileMenuOpen ? 'Đóng menu' : 'Mở menu'}
            aria-expanded={mobileMenuOpen}
          >
            <span />
            <span />
            <span />
          </button>
        </div>
      </div>
    </header>
  );
}

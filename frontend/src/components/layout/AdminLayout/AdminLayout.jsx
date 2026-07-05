import { Outlet, useLocation } from 'react-router-dom';
import { Link } from 'react-router-dom';
import styles from './AdminLayout.module.css';

const ADMIN_MENU_ITEMS = [
  { path: '/admin', label: 'Dashboard' },
  { path: '/admin/users', label: 'Người dùng' },
  { path: '/admin/documents', label: 'Văn bản' },
  { path: '/admin/categories', label: 'Danh mục' },
  { path: '/admin/tags', label: 'Tags' },
  // { path: '/admin/crawl', label: 'Crawl' }, // Hidden temporarily
];

export function AdminLayout() {
  const location = useLocation();

  return (
    <div className={styles.layout}>
      <aside className={styles.sidebar}>
        <div className={styles.sidebar__header}>
          <h2 className={styles.sidebar__title}>Admin</h2>
        </div>
        <nav className={styles.sidebar__nav}>
          <ul className={styles.navList}>
            {ADMIN_MENU_ITEMS.map((item) => (
              <li key={item.path} className={styles.navItem}>
                <Link
                  to={item.path}
                  className={`${styles.navLink} ${
                    location.pathname === item.path ? styles.navLink__active : ''
                  }`}
                >
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        <div className={styles.sidebar__footer}>
          <Link to="/" className={styles.backLink}>
            ← Trang chủ
          </Link>
        </div>
      </aside>
      <main className={styles.layout__main}>
        <Outlet />
      </main>
    </div>
  );
}

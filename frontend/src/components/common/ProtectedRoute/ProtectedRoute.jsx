import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore, selectIsAuthenticated, selectIsAdmin } from '@/stores/authStore';
import { Spinner } from '../Spinner/Spinner';
import styles from './ProtectedRoute.module.css';

export function ProtectedRoute({ children, requireAdmin = false }) {
  const sessionChecked = useAuthStore((s) => s.sessionChecked);
  const isAuthenticated = useAuthStore(selectIsAuthenticated);
  const isAdmin = useAuthStore(selectIsAdmin);
  const location = useLocation();

  if (!sessionChecked) {
    return (
      <div className={styles.loading}>
        <Spinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
}

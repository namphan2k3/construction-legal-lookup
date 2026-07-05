import { useEffect, Suspense, lazy } from 'react';
import { Routes, Route } from 'react-router-dom';
import { MainLayout } from '@/components/layout/MainLayout/MainLayout';
import { AdminLayout } from '@/components/layout/AdminLayout/AdminLayout';
import { ProtectedRoute } from '@/components/common/ProtectedRoute/ProtectedRoute';
import { Spinner } from '@/components/common/Spinner/Spinner';
import { useAuthStore } from '@/stores/authStore';

import HomePage from '@/pages/public/HomePage';
import SearchPage from '@/pages/public/SearchPage';
import DocumentDetailPage from '@/pages/public/DocumentDetailPage';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import ProfilePage from '@/pages/user/ProfilePage';
import BookmarksPage from '@/pages/user/BookmarksPage';
import HistoryPage from '@/pages/user/HistoryPage';
import NotFoundPage from '@/pages/NotFoundPage';

const AdminDashboardPage = lazy(() => import('@/pages/admin/AdminDashboardPage'));
const AdminUsersPage = lazy(() => import('@/pages/admin/AdminUsersPage'));
const AdminDocumentsPage = lazy(() => import('@/pages/admin/AdminDocumentsPage'));
const AdminCategoriesPage = lazy(() => import('@/pages/admin/AdminCategoriesPage'));
const AdminTagsPage = lazy(() => import('@/pages/admin/AdminTagsPage'));
const AdminCrawlPage = lazy(() => import('@/pages/admin/AdminCrawlPage'));

function PageLoader() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem 0' }}>
      <Spinner size="lg" />
    </div>
  );
}

export default function App() {
  const tryRestoreSession = useAuthStore((s) => s.tryRestoreSession);

  useEffect(() => {
    tryRestoreSession();
  }, [tryRestoreSession]);

  return (
    <Routes>
      <Route path="/" element={<MainLayout />}>
        <Route index element={<HomePage />} />
        <Route path="search" element={<SearchPage />} />
        <Route path="documents/:id" element={<DocumentDetailPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        <Route
          path="profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="bookmarks"
          element={
            <ProtectedRoute>
              <BookmarksPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="history/search"
          element={
            <ProtectedRoute>
              <HistoryPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="admin"
          element={
            <ProtectedRoute requireAdmin>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={
            <Suspense fallback={<PageLoader />}>
              <AdminDashboardPage />
            </Suspense>
          } />
          <Route path="users" element={
            <Suspense fallback={<PageLoader />}>
              <AdminUsersPage />
            </Suspense>
          } />
          <Route path="documents" element={
            <Suspense fallback={<PageLoader />}>
              <AdminDocumentsPage />
            </Suspense>
          } />
          <Route path="categories" element={
            <Suspense fallback={<PageLoader />}>
              <AdminCategoriesPage />
            </Suspense>
          } />
          <Route path="tags" element={
            <Suspense fallback={<PageLoader />}>
              <AdminTagsPage />
            </Suspense>
          } />
          <Route path="crawl" element={
            <Suspense fallback={<PageLoader />}>
              <AdminCrawlPage />
            </Suspense>
          } />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

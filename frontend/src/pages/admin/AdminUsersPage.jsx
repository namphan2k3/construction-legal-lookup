import { useState, useEffect } from 'react';
import { Card } from '@/components/common/Card/Card';
import { Button } from '@/components/common/Button/Button';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Badge } from '@/components/common/Badge/Badge';
import { Modal } from '@/components/common/Modal/Modal';
import { ConfirmModal } from '@/components/common/ConfirmModal/ConfirmModal';
import { getUsers, disableUser, enableUser, updateUserRole, createUser } from '@/services/admin.service';
import { formatDate } from '@/utils/formatters';
import styles from './AdminUsersPage.module.css';

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    q: '',
    role: '',
    enabled: '',
  });
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createFormData, setCreateFormData] = useState({
    email: '',
    password: '',
    fullName: '',
    role: 'USER',
  });
  const [confirmModal, setConfirmModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: null,
  });

  useEffect(() => {
    loadUsers();
  }, [page, filters]);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const data = await getUsers(page, 20, filters.q, filters.enabled === '' ? null : filters.enabled === 'true', filters.role);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error('Failed to load users:', error);
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDisable = async (userId) => {
    setConfirmModal({
      isOpen: true,
      title: 'Khóa tài khoản',
      message: 'Bạn có chắc muốn khóa tài khoản này?',
      onConfirm: async () => {
        try {
          await disableUser(userId);
          loadUsers();
          setConfirmModal({ isOpen: false, title: '', message: '', onConfirm: null });
        } catch (error) {
          console.error('Failed to disable user:', error);
          alert('Không thể khóa tài khoản');
        }
      },
    });
  };

  const handleEnable = async (userId) => {
    try {
      await enableUser(userId);
      loadUsers();
    } catch (error) {
      console.error('Failed to enable user:', error);
      alert('Không thể mở khóa tài khoản');
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    setConfirmModal({
      isOpen: true,
      title: 'Đổi role',
      message: `Bạn có chắc muốn đổi role thành ${newRole}?`,
      onConfirm: async () => {
        try {
          await updateUserRole(userId, newRole);
          loadUsers();
          setConfirmModal({ isOpen: false, title: '', message: '', onConfirm: null });
        } catch (error) {
          console.error('Failed to update role:', error);
          alert('Không thể đổi role');
        }
      },
    });
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      await createUser(createFormData);
      setShowCreateForm(false);
      setCreateFormData({ email: '', password: '', fullName: '', role: 'USER' });
      loadUsers();
    } catch (error) {
      console.error('Failed to create user:', error);
      alert('Không thể tạo user');
    }
  };

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <div className={styles.header__top}>
            <div>
              <h1 className="page__title">Quản lý người dùng</h1>
              <p className="page__subtitle">
                Quản lý tài khoản, phân quyền và trạng thái người dùng.
              </p>
            </div>
            <Button variant="primary" onClick={() => setShowCreateForm(true)}>
              Tạo user mới
            </Button>
          </div>
        </header>

        <Card padding="md">
          <div className={styles.filters}>
            <input
              type="text"
              placeholder="Tìm kiếm email hoặc tên..."
              value={filters.q}
              onChange={(e) => setFilters({ ...filters, q: e.target.value })}
              className={styles.searchInput}
            />
            <select
              value={filters.role}
              onChange={(e) => setFilters({ ...filters, role: e.target.value })}
              className={styles.select}
            >
              <option value="">Tất cả role</option>
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <select
              value={filters.enabled}
              onChange={(e) => setFilters({ ...filters, enabled: e.target.value })}
              className={styles.select}
            >
              <option value="">Tất cả trạng thái</option>
              <option value="true">Đang hoạt động</option>
              <option value="false">Đã khóa</option>
            </select>
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setFilters({ q: '', role: '', enabled: '' })}
            >
              Đặt lại
            </Button>
          </div>
        </Card>

        {loading ? (
          <LoadingOverlay message="Đang tải dữ liệu..." />
        ) : (
          <>
            <Card padding="md">
              {users.length > 0 ? (
                <div className={styles.table}>
                  <div className={styles.tableHeader}>
                    <div className={styles.tableCell}>Email</div>
                    <div className={styles.tableCell}>Họ tên</div>
                    <div className={styles.tableCell}>Role</div>
                    <div className={styles.tableCell}>Trạng thái</div>
                    <div className={styles.tableCell}>Ngày tạo</div>
                    <div className={styles.tableCell}>Thao tác</div>
                  </div>
                  {users.map((user) => (
                    <div key={user.id} className={styles.tableRow}>
                      <div className={styles.tableCell}>
                        <span className={styles.email}>{user.email}</span>
                      </div>
                      <div className={styles.tableCell}>{user.fullName || '-'}</div>
                      <div className={styles.tableCell}>
                        <Badge variant={user.role === 'ADMIN' ? 'primary' : 'secondary'}>
                          {user.role}
                        </Badge>
                      </div>
                      <div className={styles.tableCell}>
                        <Badge variant={user.enabled ? 'success' : 'danger'}>
                          {user.enabled ? 'Đang hoạt động' : 'Đã khóa'}
                        </Badge>
                      </div>
                      <div className={styles.tableCell}>
                        {formatDate(user.createdAt)}
                      </div>
                      <div className={styles.tableCell}>
                        <div className={styles.actions}>
                          <select
                            value={user.role}
                            onChange={(e) => handleRoleChange(user.id, e.target.value)}
                            className={styles.roleSelect}
                          >
                            <option value="USER">USER</option>
                            <option value="ADMIN">ADMIN</option>
                          </select>
                          {user.enabled ? (
                            <Button
                              variant="danger"
                              size="sm"
                              onClick={() => handleDisable(user.id)}
                            >
                              Khóa
                            </Button>
                          ) : (
                            <Button
                              variant="success"
                              size="sm"
                              onClick={() => handleEnable(user.id)}
                            >
                              Mở khóa
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className={styles.emptyText}>Không tìm thấy người dùng nào.</p>
              )}
            </Card>

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
      </div>

      <Modal
        isOpen={showCreateForm}
        onClose={() => setShowCreateForm(false)}
        title="Tạo user mới"
        size="md"
      >
        <form onSubmit={handleCreateUser} className={styles.modalForm}>
          <div className={styles.formGrid}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Email *</label>
              <input
                type="email"
                required
                value={createFormData.email}
                onChange={(e) => setCreateFormData({ ...createFormData, email: e.target.value })}
                className={styles.formInput}
                placeholder="user@example.com"
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Mật khẩu *</label>
              <input
                type="password"
                required
                minLength={6}
                value={createFormData.password}
                onChange={(e) => setCreateFormData({ ...createFormData, password: e.target.value })}
                className={styles.formInput}
                placeholder="Ít nhất 6 ký tự"
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Họ tên *</label>
              <input
                type="text"
                required
                value={createFormData.fullName}
                onChange={(e) => setCreateFormData({ ...createFormData, fullName: e.target.value })}
                className={styles.formInput}
                placeholder="Nguyễn Văn A"
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Role</label>
              <select
                value={createFormData.role}
                onChange={(e) => setCreateFormData({ ...createFormData, role: e.target.value })}
                className={styles.formInput}
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          </div>
          <div className={styles.formActions}>
            <Button type="submit" variant="primary">
              Tạo user
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => setShowCreateForm(false)}
            >
              Hủy
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        onClose={() => setConfirmModal({ isOpen: false, title: '', message: '', onConfirm: null })}
        onConfirm={confirmModal.onConfirm}
        title={confirmModal.title}
        message={confirmModal.message}
        confirmText="Xác nhận"
        cancelText="Hủy"
        variant="danger"
      />
    </div>
  );
}

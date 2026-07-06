import { useState, useEffect } from 'react';
import { Card } from '@/components/common/Card/Card';
import { Button } from '@/components/common/Button/Button';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Badge } from '@/components/common/Badge/Badge';
import { Modal } from '@/components/common/Modal/Modal';
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from '@/services/admin.service';
import { formatDate } from '@/utils/formatters';
import styles from './AdminCategoriesPage.module.css';

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [formData, setFormData] = useState({
    name: '',
    slug: '',
    description: '',
    displayOrder: 0,
  });

  useEffect(() => {
    loadCategories();
  }, [searchQuery]);

  const loadCategories = async () => {
    setLoading(true);
    try {
      const data = await getCategories();
      const filtered = searchQuery 
        ? data.filter(cat => 
            cat.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            cat.slug.toLowerCase().includes(searchQuery.toLowerCase())
          )
        : data;
      setCategories(filtered || []);
    } catch (error) {
      console.error('Failed to load categories:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await updateCategory(editingId, formData);
      } else {
        await createCategory(formData);
      }
      setFormData({ name: '', slug: '', description: '', displayOrder: 0 });
      setEditingId(null);
      loadCategories();
    } catch (error) {
      console.error('Failed to save category:', error);
      alert('Không thể lưu danh mục');
    }
  };

  const handleEdit = (category) => {
    setEditingId(category.id);
    setFormData({
      name: category.name,
      slug: category.slug,
      description: category.description || '',
      displayOrder: category.displayOrder || 0,
    });
    setShowModal(true);
  };

  const handleCreate = () => {
    setEditingId(null);
    setFormData({ name: '', slug: '', description: '', displayOrder: 0 });
    setShowModal(true);
  };

  const handleCancel = () => {
    setShowModal(false);
    setEditingId(null);
    setFormData({ name: '', slug: '', description: '', displayOrder: 0 });
  };

  const handleDelete = async (id) => {
    if (!confirm('Bạn có chắc muốn xóa danh mục này?')) return;
    try {
      await deleteCategory(id);
      loadCategories();
    } catch (error) {
      console.error('Failed to delete category:', error);
      alert('Không thể xóa danh mục');
    }
  };

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <div className={styles.header__top}>
            <div>
              <h1 className="page__title">Quản lý danh mục</h1>
              <p className="page__subtitle">
                Quản lý danh mục phân loại văn bản pháp luật.
              </p>
            </div>
            <Button variant="primary" onClick={handleCreate}>
              Tạo danh mục mới
            </Button>
          </div>
        </header>

        <Card padding="md">
          <div className={styles.searchSection}>
            <input
              type="text"
              placeholder="Tìm kiếm danh mục..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className={styles.searchInput}
            />
          </div>
        </Card>

        {loading ? (
          <LoadingOverlay message="Đang tải dữ liệu..." />
        ) : (
          <Card padding="md">
            {categories.length > 0 ? (
              <div className={styles.list}>
                {categories.map((category) => (
                  <div key={category.id} className={styles.item}>
                    <div className={styles.item__main}>
                      <h4 className={styles.item__name}>{category.name}</h4>
                      <p className={styles.item__slug}>/{category.slug}</p>
                      {category.description && (
                        <p className={styles.item__desc}>{category.description}</p>
                      )}
                    </div>
                    <div className={styles.item__meta}>
                      <Badge variant="secondary">Thứ tự: {category.displayOrder}</Badge>
                      {category.documentCount !== undefined && (
                        <Badge variant="primary">{category.documentCount} văn bản</Badge>
                      )}
                      <div className={styles.item__actions}>
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => handleEdit(category)}
                        >
                          Sửa
                        </Button>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleDelete(category.id)}
                        >
                          Xóa
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className={styles.emptyText}>Chưa có danh mục nào.</p>
            )}
          </Card>
        )}

        <Modal
          isOpen={showModal}
          onClose={handleCancel}
          title={editingId ? 'Sửa danh mục' : 'Tạo danh mục mới'}
          size="lg"
        >
          <form onSubmit={handleSubmit} className={styles.modalForm}>
            <div className={styles.formGrid}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Tên danh mục *</label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className={styles.input}
                  placeholder="VD: Giấy phép xây dựng"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.label}>Slug *</label>
                <input
                  type="text"
                  required
                  value={formData.slug}
                  onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
                  className={styles.input}
                  placeholder="VD: giay-phep-xay-dung"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.label}>Thứ tự hiển thị</label>
                <input
                  type="number"
                  value={formData.displayOrder}
                  onChange={(e) => setFormData({ ...formData, displayOrder: parseInt(e.target.value) || 0 })}
                  className={styles.input}
                />
              </div>
              <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                <label className={styles.label}>Mô tả</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className={styles.textarea}
                  rows={3}
                  placeholder="Mô tả về danh mục..."
                />
              </div>
            </div>
            <div className={styles.formActions}>
              <Button type="submit" variant="primary">
                {editingId ? 'Cập nhật' : 'Tạo mới'}
              </Button>
              <Button type="button" variant="secondary" onClick={handleCancel}>
                Hủy
              </Button>
            </div>
          </form>
        </Modal>
      </div>
    </div>
  );
}

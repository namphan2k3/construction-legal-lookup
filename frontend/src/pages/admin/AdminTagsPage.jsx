import { useState, useEffect } from 'react';
import { Card } from '@/components/common/Card/Card';
import { Button } from '@/components/common/Button/Button';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Badge } from '@/components/common/Badge/Badge';
import {
  getTags,
  createTag,
  updateTag,
  deleteTag,
} from '@/services/admin.service';
import { formatDate } from '@/utils/formatters';
import styles from './AdminTagsPage.module.css';

export default function AdminTagsPage() {
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    slug: '',
  });

  useEffect(() => {
    loadTags();
  }, []);

  const loadTags = async () => {
    setLoading(true);
    try {
      const data = await getTags();
      setTags(data || []);
    } catch (error) {
      console.error('Failed to load tags:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await updateTag(editingId, formData);
      } else {
        await createTag(formData);
      }
      setFormData({ name: '', slug: '' });
      setEditingId(null);
      loadTags();
    } catch (error) {
      console.error('Failed to save tag:', error);
      alert('Không thể lưu tag');
    }
  };

  const handleEdit = (tag) => {
    setEditingId(tag.id);
    setFormData({
      name: tag.name,
      slug: tag.slug,
    });
  };

  const handleDelete = async (id) => {
    if (!confirm('Bạn có chắc muốn xóa tag này?')) return;
    try {
      await deleteTag(id);
      loadTags();
    } catch (error) {
      console.error('Failed to delete tag:', error);
      alert('Không thể xóa tag');
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setFormData({ name: '', slug: '' });
  };

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <h1 className="page__title">Quản lý Tags</h1>
          <p className="page__subtitle">
            Quản lý tags gán cho văn bản pháp luật.
          </p>
        </header>

        <Card padding="md">
          <form onSubmit={handleSubmit} className={styles.form}>
            <h3 className={styles.formTitle}>
              {editingId ? 'Sửa tag' : 'Tạo tag mới'}
            </h3>
            <div className={styles.formGrid}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Tên tag *</label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className={styles.input}
                  placeholder="VD: GPXD"
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
                  placeholder="VD: gpxd"
                />
              </div>
            </div>
            <div className={styles.formActions}>
              <Button type="submit" variant="primary">
                {editingId ? 'Cập nhật' : 'Tạo mới'}
              </Button>
              {editingId && (
                <Button type="button" variant="secondary" onClick={handleCancel}>
                  Hủy
                </Button>
              )}
            </div>
          </form>
        </Card>

        {loading ? (
          <LoadingOverlay message="Đang tải dữ liệu..." />
        ) : (
          <Card padding="md">
            {tags.length > 0 ? (
              <div className={styles.list}>
                {tags.map((tag) => (
                  <div key={tag.id} className={styles.item}>
                    <div className={styles.item__main}>
                      <h4 className={styles.item__name}>{tag.name}</h4>
                      <p className={styles.item__slug}>/{tag.slug}</p>
                    </div>
                    <div className={styles.item__meta}>
                      {tag.documentCount !== undefined && (
                        <Badge variant="primary">{tag.documentCount} văn bản</Badge>
                      )}
                      <div className={styles.item__actions}>
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => handleEdit(tag)}
                        >
                          Sửa
                        </Button>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleDelete(tag.id)}
                        >
                          Xóa
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className={styles.emptyText}>Chưa có tag nào.</p>
            )}
          </Card>
        )}
      </div>
    </div>
  );
}

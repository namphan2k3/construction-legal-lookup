import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from '@/components/common/Card/Card';
import { Button } from '@/components/common/Button/Button';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Badge } from '@/components/common/Badge/Badge';
import { Modal } from '@/components/common/Modal/Modal';
import {
  getAdminDocuments,
  deleteDocument,
  restoreDocument,
  createDocument,
} from '@/services/admin.service';
import { formatDate, getDocumentTypeLabel, getStatusLabel, getStatusVariant } from '@/utils/formatters';
import styles from './AdminDocumentsPage.module.css';

export default function AdminDocumentsPage() {
  const navigate = useNavigate();
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [includeDeleted, setIncludeDeleted] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createFormData, setCreateFormData] = useState({
    documentNumber: '',
    title: '',
    abstractText: '',
    documentType: 'LUAT',
    issuingBody: '',
    signer: '',
    issuedDate: '',
    effectiveDate: '',
    expiryDate: '',
    status: 'CON_HIEU_LUC',
    pdfFile: null,
    sourceUrl: '',
    categoryIds: [],
    tagIds: [],
  });

  useEffect(() => {
    loadDocuments();
  }, [page, includeDeleted]);

  const loadDocuments = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 20,
        includeDeleted,
      };
      const data = await getAdminDocuments(params);
      setDocuments(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error('Failed to load documents:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Bạn có chắc muốn xóa văn bản này?')) return;
    try {
      await deleteDocument(id);
      loadDocuments();
    } catch (error) {
      console.error('Failed to delete document:', error);
      alert('Không thể xóa văn bản');
    }
  };

  const handleRestore = async (id) => {
    try {
      await restoreDocument(id);
      loadDocuments();
    } catch (error) {
      console.error('Failed to restore document:', error);
      alert('Không thể khôi phục văn bản');
    }
  };

  const handleCreateDocument = async (e) => {
    e.preventDefault();
    try {
      const formData = new FormData();
      formData.append('documentNumber', createFormData.documentNumber);
      formData.append('title', createFormData.title);
      formData.append('abstractText', createFormData.abstractText);
      formData.append('documentType', createFormData.documentType);
      formData.append('issuingBody', createFormData.issuingBody);
      formData.append('signer', createFormData.signer);
      formData.append('issuedDate', createFormData.issuedDate);
      formData.append('effectiveDate', createFormData.effectiveDate);
      formData.append('expiryDate', createFormData.expiryDate);
      formData.append('status', createFormData.status);
      formData.append('file', createFormData.pdfFile);
      formData.append('sourceUrl', createFormData.sourceUrl);
      createFormData.categoryIds.forEach(id => formData.append('categoryIds', id));
      createFormData.tagIds.forEach(id => formData.append('tagIds', id));

      const created = await createDocument(formData);
      setShowCreateForm(false);
      setCreateFormData({
        documentNumber: '',
        title: '',
        abstractText: '',
        documentType: 'LUAT',
        issuingBody: '',
        signer: '',
        issuedDate: '',
        effectiveDate: '',
        expiryDate: '',
        status: 'CON_HIEU_LUC',
        pdfFile: null,
        sourceUrl: '',
        categoryIds: [],
        tagIds: [],
      });
      loadDocuments();
      navigate(`/documents/${created.id}`);
    } catch (error) {
      console.error('Failed to create document:', error);
      alert('Không thể tạo văn bản');
    }
  };

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <div className={styles.header__top}>
            <div>
              <h1 className="page__title">Quản lý văn bản</h1>
              <p className="page__subtitle">
                Quản lý, tạo, sửa và xóa văn bản pháp luật.
              </p>
            </div>
            <Button variant="primary" onClick={() => setShowCreateForm(true)}>
              Tạo văn bản mới
            </Button>
          </div>
        </header>

        <Card padding="md">
          <div className={styles.filters}>
            <label className={styles.checkboxLabel}>
              <input
                type="checkbox"
                checked={includeDeleted}
                onChange={(e) => setIncludeDeleted(e.target.checked)}
                className={styles.checkbox}
              />
              Hiển thị văn bản đã xóa
            </label>
          </div>
        </Card>

        {loading ? (
          <LoadingOverlay message="Đang tải dữ liệu..." />
        ) : (
          <>
            <Card padding="md">
              {documents.length > 0 ? (
                <div className={styles.table}>
                  <div className={styles.tableHeader}>
                    <div className={styles.tableCell}>Số hiệu</div>
                    <div className={styles.tableCell}>Tiêu đề</div>
                    <div className={styles.tableCell}>Loại</div>
                    <div className={styles.tableCell}>Cơ quan</div>
                    <div className={styles.tableCell}>Trạng thái</div>
                    <div className={styles.tableCell}>Ngày ban hành</div>
                    <div className={styles.tableCell}>Thao tác</div>
                  </div>
                  {documents.map((doc) => (
                    <div key={doc.id} className={styles.tableRow}>
                      <div className={styles.tableCell}>
                        <span className={styles.docNumber}>{doc.documentNumber}</span>
                      </div>
                      <div className={styles.tableCell}>
                        <Link to={`/documents/${doc.id}`} className={styles.docTitle}>
                          {doc.title}
                        </Link>
                      </div>
                      <div className={styles.tableCell}>
                        <Badge variant="primary">{getDocumentTypeLabel(doc.documentType)}</Badge>
                      </div>
                      <div className={styles.tableCell}>{doc.issuingBody || '-'}</div>
                      <div className={styles.tableCell}>
                        <Badge variant={getStatusVariant(doc.status)}>
                          {getStatusLabel(doc.status)}
                        </Badge>
                        {doc.deletedAt && (
                          <Badge variant="danger" className={styles.deletedBadge}>
                            Đã xóa
                          </Badge>
                        )}
                      </div>
                      <div className={styles.tableCell}>
                        {formatDate(doc.issuedDate)}
                      </div>
                      <div className={styles.tableCell}>
                        <div className={styles.actions}>
                          <Button variant="secondary" size="sm">
                            Sửa
                          </Button>
                          {doc.deletedAt ? (
                            <Button
                              variant="success"
                              size="sm"
                              onClick={() => handleRestore(doc.id)}
                            >
                              Khôi phục
                            </Button>
                          ) : (
                            <Button
                              variant="danger"
                              size="sm"
                              onClick={() => handleDelete(doc.id)}
                            >
                              Xóa
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className={styles.emptyText}>Không tìm thấy văn bản nào.</p>
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
        title="Tạo văn bản mới"
        size="xl"
      >
        <form onSubmit={handleCreateDocument} className={styles.modalForm}>
          <div className={styles.formGrid}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Số hiệu *</label>
              <input
                type="text"
                required
                value={createFormData.documentNumber}
                onChange={(e) => setCreateFormData({ ...createFormData, documentNumber: e.target.value })}
                className={styles.formInput}
                placeholder="VD: 123/2024/BXD"
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Tiêu đề *</label>
              <input
                type="text"
                required
                value={createFormData.title}
                onChange={(e) => setCreateFormData({ ...createFormData, title: e.target.value })}
                className={styles.formInput}
                placeholder="Tiêu đề văn bản"
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Loại văn bản *</label>
              <select
                required
                value={createFormData.documentType}
                onChange={(e) => setCreateFormData({ ...createFormData, documentType: e.target.value })}
                className={styles.formInput}
              >
                <option value="LUAT">Luật</option>
                <option value="NGHI_DINH">Nghị định</option>
                <option value="THONG_TU">Thông tư</option>
                <option value="QUYET_DINH">Quyết định</option>
                <option value="QCVN">QCVN</option>
                <option value="TCVN">TCVN</option>
                <option value="CONG_VAN">Công văn</option>
                <option value="KHAC">Khác</option>
              </select>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Cơ quan ban hành</label>
              <input
                type="text"
                value={createFormData.issuingBody}
                onChange={(e) => setCreateFormData({ ...createFormData, issuingBody: e.target.value })}
                className={styles.formInput}
                placeholder="Bộ Xây dựng"
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Người ký</label>
              <input
                type="text"
                value={createFormData.signer}
                onChange={(e) => setCreateFormData({ ...createFormData, signer: e.target.value })}
                className={styles.formInput}
                placeholder="Tên người ký"
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Ngày ban hành *</label>
              <input
                type="date"
                required
                value={createFormData.issuedDate}
                onChange={(e) => setCreateFormData({ ...createFormData, issuedDate: e.target.value })}
                className={styles.formInput}
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Ngày hiệu lực</label>
              <input
                type="date"
                value={createFormData.effectiveDate}
                onChange={(e) => setCreateFormData({ ...createFormData, effectiveDate: e.target.value })}
                className={styles.formInput}
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Ngày hết hiệu lực</label>
              <input
                type="date"
                value={createFormData.expiryDate}
                onChange={(e) => setCreateFormData({ ...createFormData, expiryDate: e.target.value })}
                className={styles.formInput}
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Trạng thái *</label>
              <select
                required
                value={createFormData.status}
                onChange={(e) => setCreateFormData({ ...createFormData, status: e.target.value })}
                className={styles.formInput}
              >
                <option value="CON_HIEU_LUC">Còn hiệu lực</option>
                <option value="HET_HIEU_LUC">Hết hiệu lực</option>
                <option value="CHUA_CO_HIEU_LUC">Chưa có hiệu lực</option>
                <option value="HET_HIEU_LUC_MOT_PHAN">Hết hiệu lực một phần</option>
              </select>
            </div>
            <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
              <label className={styles.formLabel}>File PDF *</label>
              <input
                type="file"
                accept=".pdf"
                required
                onChange={(e) => setCreateFormData({ ...createFormData, pdfFile: e.target.files[0] })}
                className={styles.formInput}
              />
              <small style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>
                Upload file PDF. Backend sẽ tự động trích xuất nội dung.
              </small>
            </div>
            <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
              <label className={styles.formLabel}>Nguồn</label>
              <input
                type="url"
                value={createFormData.sourceUrl}
                onChange={(e) => setCreateFormData({ ...createFormData, sourceUrl: e.target.value })}
                className={styles.formInput}
                placeholder="https://..."
              />
            </div>
          </div>
          <div className={styles.formActions}>
            <Button type="submit" variant="primary">
              Tạo văn bản
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
    </div>
  );
}

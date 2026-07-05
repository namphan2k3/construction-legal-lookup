import { useEffect, useState } from 'react';
import { DocumentCard } from '@/components/document/DocumentCard/DocumentCard';
import { CardGrid } from '@/components/common/Card/Card';
import { Pagination } from '@/components/common/Pagination/Pagination';
import { EmptyState } from '@/components/common/EmptyState/EmptyState';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { getBookmarks } from '@/services/bookmark.service';
import { PAGE_SIZE } from '@/utils/constants';

export default function BookmarksPage() {
  const [bookmarks, setBookmarks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    getBookmarks(page, PAGE_SIZE)
      .then((data) => {
        setBookmarks(data.content || []);
        setTotalPages(data.totalPages || 0);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <h1 className="page__title">Văn bản yêu thích</h1>
          <p className="page__subtitle">Danh sách văn bản bạn đã lưu để xem lại sau</p>
        </header>

        {loading ? (
          <LoadingOverlay message="Đang tải..." />
        ) : bookmarks.length > 0 ? (
          <>
            <CardGrid columns={3}>
              {bookmarks.map((item) => (
                <DocumentCard key={item.id} document={item.document} />
              ))}
            </CardGrid>
            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
          </>
        ) : (
          <EmptyState
            title="Chưa có văn bản yêu thích"
            description="Khi xem chi tiết văn bản, nhấn Lưu văn bản để thêm vào danh sách này."
            actionLabel="Tra cứu văn bản"
            actionTo="/search"
          />
        )}
      </div>
    </div>
  );
}

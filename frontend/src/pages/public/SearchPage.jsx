import { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { SearchBar } from '@/components/search/SearchBar/SearchBar';
import { FilterPanel } from '@/components/search/FilterPanel/FilterPanel';
import { DocumentCard } from '@/components/document/DocumentCard/DocumentCard';
import { CardGrid } from '@/components/common/Card/Card';
import { Pagination } from '@/components/common/Pagination/Pagination';
import { EmptyState } from '@/components/common/EmptyState/EmptyState';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { Button } from '@/components/common/Button/Button';
import { useUIStore } from '@/stores/uiStore';
import { searchDocuments, getFilters } from '@/services/document.service';
import { PAGE_SIZE } from '@/utils/constants';
import styles from './SearchPage.module.css';

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const filterDrawerOpen = useUIStore((s) => s.filterDrawerOpen);
  const toggleFilterDrawer = useUIStore((s) => s.toggleFilterDrawer);
  const closeFilterDrawer = useUIStore((s) => s.closeFilterDrawer);

  const [results, setResults] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [availableFilters, setAvailableFilters] = useState(null);

  const q = searchParams.get('q') || '';
  const page = Number(searchParams.get('page') || 0);

  const filters = {
    type: searchParams.get('type') || undefined,
    status: searchParams.get('status') || undefined,
    issuingBody: searchParams.get('issuingBody') || undefined,
    year: searchParams.get('year') ? Number(searchParams.get('year')) : undefined,
    categoryId: searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined,
    tagId: searchParams.get('tagId') ? Number(searchParams.get('tagId')) : undefined,
  };

  useEffect(() => {
    getFilters().then(setAvailableFilters).catch(console.error);
  }, []);

  const runSearch = useCallback(async () => {
    setLoading(true);
    try {
      const data = await searchDocuments({
        q: q || undefined,
        ...filters,
        page,
        size: PAGE_SIZE,
        sort: searchParams.get('sort') || undefined,
      });
      setResults(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error(err);
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, [q, page, filters.type, filters.status, filters.issuingBody, filters.year, filters.categoryId, filters.tagId, searchParams.get('sort')]);

  useEffect(() => {
    runSearch();
  }, [runSearch]);

  const updateParams = (updates) => {
    const next = new URLSearchParams(searchParams);
    Object.entries(updates).forEach(([key, value]) => {
      if (value === undefined || value === '' || value === null) {
        next.delete(key);
      } else {
        next.set(key, String(value));
      }
    });
    if (!('page' in updates)) next.set('page', '0');
    setSearchParams(next);
  };

  const handleSearch = (query) => updateParams({ q: query, page: 0 });
  const handleFilterChange = (changes) => updateParams(changes);
  const handleReset = () => setSearchParams(q ? { q } : {});
  const handlePageChange = (newPage) => updateParams({ page: newPage });

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <h1 className="page__title">Tra cứu văn bản</h1>
          <p className="page__subtitle">
            Tìm theo từ khóa, số hiệu hoặc kết hợp bộ lọc để thu hẹp kết quả.
          </p>
        </header>

        <div className={styles.searchTop}>
          <SearchBar initialQuery={q} onSearch={handleSearch} />
          <Button
            variant="secondary"
            className={styles.filterToggle}
            onClick={toggleFilterDrawer}
          >
            Bộ lọc
          </Button>
        </div>

        <div className={styles.layout}>
          <div className={`${styles.sidebar} ${filterDrawerOpen ? styles['sidebar--open'] : ''}`}>
            <div className={styles.sidebar__overlay} onClick={closeFilterDrawer} aria-hidden="true" />
            <FilterPanel
              filters={filters}
              availableFilters={availableFilters}
              onFilterChange={handleFilterChange}
              onReset={handleReset}
            />
          </div>

          <div className={styles.results}>
            {!loading && (
              <p className={styles.results__count}>
                {totalElements > 0
                  ? `Tìm thấy ${totalElements} văn bản`
                  : 'Không có kết quả phù hợp'}
              </p>
            )}

            {loading ? (
              <LoadingOverlay message="Đang tìm kiếm..." />
            ) : results.length > 0 ? (
              <>
                <CardGrid columns={2}>
                  {results.map((doc) => (
                    <DocumentCard key={doc.id} document={doc} showViews />
                  ))}
                </CardGrid>
                <Pagination page={page} totalPages={totalPages} onPageChange={handlePageChange} />
              </>
            ) : (
              <EmptyState
                title="Không tìm thấy văn bản"
                description="Thử đổi từ khóa hoặc bỏ bớt bộ lọc để mở rộng kết quả."
                actionLabel="Đặt lại bộ lọc"
                onAction={handleReset}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

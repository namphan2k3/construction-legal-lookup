import { Select } from '@/components/common/Input/Input';
import { Button } from '@/components/common/Button/Button';
import styles from './FilterPanel.module.css';

export function FilterPanel({ filters, onFilterChange, availableFilters, onReset }) {
  if (!availableFilters) return null;

  return (
    <aside className={styles.panel}>
      <div className={styles.panel__header}>
        <h2 className={styles.panel__title}>Bộ lọc</h2>
        <Button variant="ghost" size="sm" type="button" onClick={onReset}>
          Đặt lại
        </Button>
      </div>

      <Select
        label="Loại văn bản"
        value={filters.type || ''}
        onChange={(e) => onFilterChange({ type: e.target.value || undefined })}
      >
        <option value="">Tất cả</option>
        {(availableFilters.documentTypes || []).map((item, index) => {
          const value = typeof item === 'object' ? item.value : item;
          const label = typeof item === 'object' ? item.label : item;
          return (
            <option key={value || index} value={value}>
              {label}
            </option>
          );
        })}
      </Select>

      <Select
        label="Trạng thái hiệu lực"
        value={filters.status || ''}
        onChange={(e) => onFilterChange({ status: e.target.value || undefined })}
      >
        <option value="">Tất cả</option>
        {(availableFilters.statuses || []).map((item, index) => {
          const value = typeof item === 'object' ? item.value : item;
          const label = typeof item === 'object' ? item.label : item;
          return (
            <option key={value || index} value={value}>
              {label}
            </option>
          );
        })}
      </Select>

      <Select
        label="Cơ quan ban hành"
        value={filters.issuingBody || ''}
        onChange={(e) => onFilterChange({ issuingBody: e.target.value || undefined })}
      >
        <option value="">Tất cả</option>
        {(availableFilters.issuingBodies || []).map((body, index) => {
          const value = typeof body === 'object' ? body.value : body;
          const label = typeof body === 'object' ? body.label : body;
          return (
            <option key={value || index} value={value}>
              {label}
            </option>
          );
        })}
      </Select>

      <Select
        label="Năm ban hành"
        value={filters.year || ''}
        onChange={(e) => onFilterChange({ year: e.target.value ? Number(e.target.value) : undefined })}
      >
        <option value="">Tất cả</option>
        {(availableFilters.years || []).map((year, index) => (
          <option key={year || index} value={year}>
            {year}
          </option>
        ))}
      </Select>

      {(availableFilters.categories || []).length > 0 ? (
        <Select
          label="Danh mục"
          value={filters.categoryId || ''}
          onChange={(e) => onFilterChange({ categoryId: e.target.value ? Number(e.target.value) : undefined })}
        >
          <option value="">Tất cả</option>
          {availableFilters.categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </Select>
      ) : null}
    </aside>
  );
}

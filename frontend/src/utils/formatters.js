import { DOCUMENT_TYPES, DOCUMENT_STATUS } from './constants';

export function formatDate(dateStr) {
  if (!dateStr) return '—';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return dateStr;
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return dateStr;
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatNumber(num) {
  if (num == null) return '0';
  return new Intl.NumberFormat('vi-VN').format(num);
}

export function getDocumentTypeLabel(type) {
  return DOCUMENT_TYPES[type] || type || '—';
}

export function getStatusLabel(status) {
  return DOCUMENT_STATUS[status] || status || '—';
}

export function getStatusVariant(status) {
  switch (status) {
    case 'CON_HIEU_LUC':
      return 'success';
    case 'HET_HIEU_LUC':
      return 'danger';
    case 'CHUA_CO_HIEU_LUC':
      return 'warning';
    default:
      return 'neutral';
  }
}

export function truncate(text, maxLength = 160) {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return `${text.slice(0, maxLength).trim()}…`;
}

export function getInitials(name) {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase();
}

export function highlightText(text, highlights) {
  if (!text || !highlights) return text;
  const highlighted = highlights.title || highlights.abstract || highlights.content;
  return highlighted || text;
}

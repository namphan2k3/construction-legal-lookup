import { api } from './api';
import { FILTER_CACHE_TTL } from '@/utils/constants';

let filtersCache = null;
let filtersCacheTime = 0;

export async function searchDocuments(params) {
  const { data } = await api.get('/documents/search', { params });
  return data.data;
}

export async function suggestDocuments(q, limit = 8) {
  const { data } = await api.get('/documents/suggest', { params: { q, limit } });
  return data.data;
}

export async function getDocumentDetail(id, highlight) {
  const { data } = await api.get(`/documents/${id}`, {
    params: highlight ? { highlight } : undefined,
  });
  return data.data;
}

export async function getFilters(force = false) {
  const now = Date.now();
  if (!force && filtersCache && now - filtersCacheTime < FILTER_CACHE_TTL) {
    return filtersCache;
  }
  const { data } = await api.get('/documents/filters');
  filtersCache = data.data;
  filtersCacheTime = now;
  return filtersCache;
}

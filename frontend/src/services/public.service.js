import { api } from './api';

export async function getStats() {
  const { data } = await api.get('/public/stats');
  return data.data;
}

export async function getRecentDocuments(limit = 10) {
  const { data } = await api.get('/public/documents/recent', { params: { limit } });
  return data.data;
}

export async function getPopularDocuments(limit = 10) {
  const { data } = await api.get('/public/documents/popular', { params: { limit } });
  return data.data;
}

export async function getUpdatedDocuments(limit = 10) {
  const { data } = await api.get('/public/documents/updated', { params: { limit } });
  return data.data;
}

export async function getCategories() {
  const { data } = await api.get('/public/categories');
  return data.data;
}

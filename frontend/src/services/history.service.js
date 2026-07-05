import { api } from './api';

export async function getSearchHistory(page = 0, size = 20) {
  const { data } = await api.get('/history/search', { params: { page, size } });
  return data.data.content || [];
}

export async function clearSearchHistory() {
  const { data } = await api.delete('/history/search');
  return data.data;
}

export async function getViewHistory(page = 0, size = 20) {
  const { data } = await api.get('/history/views', { params: { page, size } });
  return data.data.content || [];
}

export async function clearViewHistory() {
  const { data } = await api.delete('/history/views');
  return data.data;
}

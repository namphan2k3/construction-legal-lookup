import { api } from './api';

export async function getBookmarks(page = 0, size = 20) {
  const { data } = await api.get('/bookmarks', {
    params: { page, size, sort: 'createdAt,desc' },
  });
  return data.data;
}

export async function addBookmark(documentId) {
  const { data } = await api.post(`/bookmarks/${documentId}`);
  return data.data;
}

export async function removeBookmark(documentId) {
  const { data } = await api.delete(`/bookmarks/${documentId}`);
  return data.data;
}

export async function getBookmarkStatus(documentId) {
  const { data } = await api.get(`/bookmarks/${documentId}/status`);
  return data.data;
}

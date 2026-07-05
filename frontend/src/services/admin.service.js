import { api } from './api';

export async function getAdminStats(period = '7d') {
  const { data } = await api.get('/admin/dashboard', { params: { period } });
  return data.data;
}

export async function getRecentAuditLogs(limit = 10) {
  const { data } = await api.get('/admin/audit-logs', { params: { limit } });
  return data.data;
}

export async function getUsers(page = 0, size = 20, q = null, enabled = null, role = null) {
  const params = { page, size };
  if (q) params.q = q;
  if (enabled !== null) params.enabled = enabled;
  if (role) params.role = role;
  const { data } = await api.get('/admin/users', { params });
  return data.data;
}

export async function updateUserStatus(userId, status) {
  const { data } = await api.patch(`/admin/users/${userId}/status`, { status });
  return data.data;
}

export async function deleteUser(userId) {
  const { data } = await api.delete(`/admin/users/${userId}`);
  return data;
}

export async function disableUser(userId) {
  const { data } = await api.post(`/admin/users/${userId}/disable`);
  return data.data;
}

export async function enableUser(userId) {
  const { data } = await api.post(`/admin/users/${userId}/enable`);
  return data.data;
}

export async function updateUserRole(userId, role) {
  const { data } = await api.patch(`/admin/users/${userId}/role`, { role });
  return data.data;
}

export async function createUser(userData) {
  const { data } = await api.post('/admin/users', userData);
  return data.data;
}

export async function getAdminDocuments(params) {
  const { data } = await api.get('/admin/documents', { params });
  return data.data;
}

export async function createDocument(documentData) {
  const { data } = await api.post('/admin/documents', documentData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data.data;
}

export async function updateDocument(id, documentData) {
  const { data } = await api.put(`/admin/documents/${id}`, documentData);
  return data.data;
}

export async function deleteDocument(id) {
  const { data } = await api.delete(`/admin/documents/${id}`);
  return data.data;
}

export async function restoreDocument(id) {
  const { data } = await api.post(`/admin/documents/${id}/restore`);
  return data.data;
}

export async function uploadDocumentPdf(id, file) {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await api.post(`/admin/documents/${id}/upload-pdf`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data.data;
}

export async function getCategories() {
  const { data } = await api.get('/admin/categories');
  return data.data;
}

export async function createCategory(categoryData) {
  const { data } = await api.post('/admin/categories', categoryData);
  return data.data;
}

export async function updateCategory(id, categoryData) {
  const { data } = await api.put(`/admin/categories/${id}`, categoryData);
  return data.data;
}

export async function deleteCategory(id) {
  const { data } = await api.delete(`/admin/categories/${id}`);
  return data.data;
}

export async function getTags() {
  const { data } = await api.get('/admin/tags');
  return data.data;
}

export async function createTag(tagData) {
  const { data } = await api.post('/admin/tags', tagData);
  return data.data;
}

export async function updateTag(id, tagData) {
  const { data } = await api.put(`/admin/tags/${id}`, tagData);
  return data.data;
}

export async function deleteTag(id) {
  const { data } = await api.delete(`/admin/tags/${id}`);
  return data.data;
}

export async function triggerCrawl(mode = 'incremental', source = 'all') {
  const { data } = await api.post('/admin/crawl/sync', { mode, source });
  return data.data;
}

export async function getCrawlLogs(params) {
  const { data } = await api.get('/admin/crawl/logs', { params });
  return data.data;
}

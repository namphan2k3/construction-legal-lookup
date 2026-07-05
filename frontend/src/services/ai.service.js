import { api } from './api';

export async function getAiQuota() {
  const { data } = await api.get('/ai/quota');
  return data.data;
}

export async function summarizeDocument(id) {
  const { data } = await api.post(`/ai/documents/${id}/summarize`);
  return data.data;
}

export async function askDocument(id, question) {
  const { data } = await api.post(`/ai/documents/${id}/ask`, { question });
  return data.data;
}

export async function explainText(id, payload) {
  const { data } = await api.post(`/ai/documents/${id}/explain`, payload);
  return data.data;
}

import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { API_BASE_URL, REFRESH_PATH } from '@/utils/constants';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type'];
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const url = originalRequest?.url || '';

    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !url.includes(REFRESH_PATH) &&
      !url.includes('/auth/login') &&
      !url.includes('/auth/register')
    ) {
      originalRequest._retry = true;
      try {
        const { data } = await axios.post(`${API_BASE_URL}${REFRESH_PATH}`, null, {
          withCredentials: true,
        });
        if (data?.success && data?.data?.accessToken) {
          useAuthStore.getState().setAuth(data.data.accessToken, data.data.user);
          originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`;
          return api(originalRequest);
        }
      } catch {
        useAuthStore.getState().logout();
      }
    }

    return Promise.reject(error);
  }
);

export function getErrorMessage(error, fallback = 'Đã có lỗi xảy ra') {
  return error?.response?.data?.message || error?.message || fallback;
}

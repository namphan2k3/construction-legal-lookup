import { api } from './api';
import { useAuthStore } from '@/stores/authStore';
import { SKIP_RESTORE_KEY } from '@/utils/constants';

export async function register(payload) {
  const { data } = await api.post('/auth/register', payload);
  if (!data?.success) throw new Error(data?.message || 'Đăng ký thất bại');
  const { user, accessToken } = data.data;
  localStorage.removeItem(SKIP_RESTORE_KEY);
  useAuthStore.getState().setAuth(accessToken, user);
  return { user, accessToken };
}

export async function login({ email, password }) {
  const { data } = await api.post('/auth/login', { email, password });
  if (!data?.success || !data?.data) throw new Error(data?.message || 'Đăng nhập thất bại');
  const { user, accessToken } = data.data;
  localStorage.removeItem(SKIP_RESTORE_KEY);
  useAuthStore.getState().setAuth(accessToken, user);
  return { user, accessToken };
}

export async function logout() {
  const token = useAuthStore.getState().accessToken;
  try {
    if (token) {
      await api.post('/auth/logout', { accessToken: token });
    }
  } finally {
    localStorage.setItem(SKIP_RESTORE_KEY, '1');
    useAuthStore.getState().logout();
  }
}

export async function getProfile() {
  const { data } = await api.get('/users/profile');
  if (!data?.success) throw new Error('Lấy thông tin thất bại');
  useAuthStore.getState().updateUser(data.data);
  return data.data;
}

export async function updateProfile(payload) {
  const { data } = await api.put('/users/profile', payload);
  if (!data?.success) throw new Error('Cập nhật thất bại');
  useAuthStore.getState().updateUser(data.data);
  return data.data;
}

export async function changePassword(payload) {
  const { data } = await api.put('/users/change-password', payload);
  if (!data?.success) throw new Error('Đổi mật khẩu thất bại');
  return data.data;
}

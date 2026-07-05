import { create } from 'zustand';
import axios from 'axios';
import { API_BASE_URL, REFRESH_PATH, SKIP_RESTORE_KEY } from '@/utils/constants';

export const useAuthStore = create((set) => ({
  user: null,
  accessToken: null,
  sessionChecked: false,

  setAuth: (accessToken, user) =>
    set({ accessToken, user, sessionChecked: true }),

  updateUser: (userUpdates) =>
    set((state) => ({
      user: state.user ? { ...state.user, ...userUpdates } : null,
    })),

  logout: () =>
    set({ accessToken: null, user: null, sessionChecked: true }),

  setSessionChecked: () => set({ sessionChecked: true }),

  tryRestoreSession: async () => {
    const { accessToken } = useAuthStore.getState();
    if (localStorage.getItem(SKIP_RESTORE_KEY) === '1') {
      set({ sessionChecked: true });
      return;
    }
    if (accessToken) {
      set({ sessionChecked: true });
      return;
    }
    try {
      const { data } = await axios.post(`${API_BASE_URL}${REFRESH_PATH}`, null, {
        withCredentials: true,
      });
      if (data?.success && data?.data?.accessToken) {
        set({
          accessToken: data.data.accessToken,
          user: data.data.user ?? null,
          sessionChecked: true,
        });
      } else {
        set({ sessionChecked: true });
      }
    } catch {
      set({ sessionChecked: true });
    }
  },
}));

export const selectIsAuthenticated = (state) => !!state.accessToken;
export const selectIsAdmin = (state) => state.user?.role === 'ADMIN';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useUIStore = create(
  persist(
    (set) => ({
      mobileMenuOpen: false,
      filterDrawerOpen: false,
      adminSidebarOpen: false,

      toggleMobileMenu: () =>
        set((state) => ({ mobileMenuOpen: !state.mobileMenuOpen })),
      closeMobileMenu: () => set({ mobileMenuOpen: false }),
      toggleFilterDrawer: () =>
        set((state) => ({ filterDrawerOpen: !state.filterDrawerOpen })),
      closeFilterDrawer: () => set({ filterDrawerOpen: false }),
      toggleAdminSidebar: () =>
        set((state) => ({ adminSidebarOpen: !state.adminSidebarOpen })),
      closeAdminSidebar: () => set({ adminSidebarOpen: false }),
    }),
    { name: 'cll-ui-storage' }
  )
);

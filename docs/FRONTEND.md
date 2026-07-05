# FRONTEND — Construction Legal Lookup

> Tài liệu hướng dẫn phát triển frontend cho hệ thống tra cứu văn bản pháp luật ngành xây dựng.  
> **Tech Stack:** React 18+ (function components, hooks) · JavaScript (ES2022+) · Vite · React Router v6 · Zustand · Axios · CSS Modules  
> **Backend API:** Spring Boot REST API · **Docs:** [API.md](./API.md) · **Database:** [DATABASE.md](./DATABASE.md)  
> **Phiên bản:** 1.0 · **Cập nhật:** 2026-07-05

---

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quán-kiến-trúc)
2. [Cấu hình môi trường](#2-cấu-hình-môi-trường)
3. [Authentication & Authorization](#3-authentication--authorization)
4. [Public Pages (không cần đăng nhập)](#4-public-pages-không-cần-đăng-nhập)
5. [User Features (cần đăng nhập)](#5-user-features-cần-đăng-nhập)
6. [Admin Dashboard](#6-admin-dashboard)
7. [Components & UI Patterns](#7-components--ui-patterns)
8. [State Management](#8-state-management)
9. [Error Handling](#9-error-handling)
10. [Performance & Optimization](#10-performance--optimization)

---

## 1. Tổng quan kiến trúc

### 1.1. Tech Stack

| Layer | Công nghệ |
|-------|-----------|
| **Framework** | React 18+ (function components, hooks) |
| **Language** | JavaScript (ES2022+) |
| **Build Tool** | Vite |
| **Styling** | CSS Modules (BEM naming convention) |
| **HTTP Client** | Axios với interceptor |
| **State Management** | Zustand |
| **Routing** | React Router v6 |
| **Forms** | React Hook Form |
| **Icons** | Lucide React |
| **Deployment** | Vercel |

**Responsive Design:**
- Mobile-first approach
- Breakpoints: Mobile (< 640px), Tablet (640px - 1024px), Desktop (> 1024px)
- Flexbox và Grid layout
- Media queries trong CSS Modules
- Touch-friendly UI cho mobile

### 1.2. Cấu trúc thư mục đề xuất

```
src/
├── components/
│   ├── layout/          # Header, Footer, Sidebar
│   ├── document/        # DocumentCard, DocumentDetail
│   ├── search/          # SearchBar, FilterPanel
│   ├── auth/            # LoginForm, RegisterForm
│   └── common/          # Button, Input, Card, Modal (reusable)
├── pages/
│   ├── public/          # Home, Search, DocumentDetail
│   ├── auth/            # Login, Register
│   ├── user/            # Profile, Bookmarks, History
│   └── admin/           # Dashboard, Documents, Users
├── hooks/
│   ├── useAuth.js       # Auth state & actions
│   ├── useApi.js        # API calls wrapper
│   └── useDebounce.js   # Search debounce
├── services/
│   ├── api.js           # Axios instance & interceptors
│   ├── auth.service.js  # Auth API calls
│   ├── document.service.js
│   └── ai.service.js
├── stores/
│   ├── authStore.js     # Zustand auth store
│   ├── documentStore.js # Zustand document store
│   └── uiStore.js       # Zustand UI store (sidebar, theme)
├── utils/
│   ├── formatters.js    # Date, number formatting
│   ├── validators.js    # Form validation
│   └── constants.js     # API constants, breakpoints
└── styles/
    ├── global.css       # Global styles, reset
    ├── variables.css    # CSS variables (colors, spacing)
    └── mixins.css       # Reusable CSS mixins
```

### 1.3. Base URL & Environment

```env
# .env.local
VITE_API_URL=http://localhost:8080/api
VITE_APP_URL=http://localhost:5173
```

Production (Vercel):

```env
VITE_API_URL=https://your-backend.onrender.com/api
VITE_APP_URL=https://your-frontend.vercel.app
```

---

## 2. Cấu hình môi trường

### 2.1. Axios Instance với Interceptor

```javascript
// src/services/api.js
import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true, // Gửi cookie refreshToken
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: thêm access token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: refresh token khi 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;

    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;

      try {
        const { data } = await api.post('/auth/refresh');
        const { accessToken } = data.data;
        localStorage.setItem('accessToken', accessToken);
        original.headers.Authorization = `Bearer ${accessToken}`;
        return api(original);
      } catch (refreshError) {
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

---

## 3. Authentication & Authorization

### 3.1. Security Architecture Pattern

**Pattern:** JWT Access Token (Memory) + Refresh Token (HTTP-only Cookie)

```
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY ARCHITECTURE                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Access Token (JWT)                                          │
│  ├─ Storage: Memory (Zustand state)                          │
│  ├─ Location: NOT in localStorage/sessionStorage            │
│  ├─ Lifetime: ~15 minutes                                   │
│  └─ Transmission: Authorization: Bearer <token> header       │
│                                                              │
│  Refresh Token                                               │
│  ├─ Storage: HTTP-only Cookie (set by backend)             │
│  ├─ Path: /api/auth/refresh (restricted scope)              │
│  ├─ Attributes: HttpOnly, Secure, SameSite=None              │
│  ├─ Lifetime: ~7 days (Redis)                                │
│  └─ Transmission: Automatic cookie by browser              │
│                                                              │
│  Logout Prevention Flag                                      │
│  ├─ Storage: localStorage (key: 'cll_skip_restore')         │
│  ├─ Purpose: Prevent auto-restore after explicit logout     │
│  └─ Lifetime: Until user clears or logs in again            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

**Why Memory Storage for Access Token?**
- **XSS Protection:** Access token in memory cannot be stolen by XSS attacks
- **CSRF Protection:** Refresh token in HTTP-only cookie cannot be accessed by JS
- **Automatic Refresh:** Axios interceptor handles token rotation transparently
- **Session Isolation:** Each browser tab/device has its own session

### 3.2. Auth Store (Zustand) - Secure Implementation

```javascript
// src/stores/authStore.js
import { create } from 'zustand';
import axios from 'axios';
import { API_BASE_URL } from '@/utils/constants';

const REFRESH_PATH = '/auth/refresh';

/**
 * Auth store – JWT access token, user info
 * AT lưu trong memory (biến JS / state) – không persist
 * Refresh token nằm trong HTTP-only cookie (backend)
 */
export const useAuthStore = create((set) => ({
  user: null,
  accessToken: null,

  setAuth: (accessToken, user) =>
    set({ accessToken, user }),

  updateUser: (userUpdates) =>
    set((state) => ({
      user: state.user ? { ...state.user, ...userUpdates } : null,
    })),

  logout: () =>
    set({ accessToken: null, user: null }),

  /** Khôi phục session từ refresh token (HTTP-only cookie) khi app load */
  tryRestoreSession: async () => {
    const { accessToken } = useAuthStore.getState();
    // Nếu user đã chủ động logout trên máy này, không tự restore lại
    if (localStorage.getItem('cll_skip_restore') === '1') return;
    if (accessToken) return; // Đã có AT trong memory
    try {
      const { data } = await axios.post(API_BASE_URL + REFRESH_PATH, null, {
        withCredentials: true,
      });
      if (data?.success && data?.data?.accessToken) {
        set({
          accessToken: data.data.accessToken,
          user: data.data.user ?? null,
        });
      }
    } catch {
      // Không có refresh cookie hoặc hết hạn – giữ logged out
    }
  },
}));

/** Selector: derived from accessToken */
export const selectIsAuthenticated = (state) => !!state.accessToken;
export const selectIsAdmin = (state) => state.user?.role === 'ADMIN';
```

### 3.3. Auth Service - API Calls

```javascript
// src/services/auth.service.js
import { api } from './api';
import { useAuthStore } from '@/stores/authStore';

/**
 * POST /api/auth/register
 * @param {{ email: string, password: string, fullName: string }}
 */
export async function register(payload) {
  const { data } = await api.post('/auth/register', payload);
  if (!data?.success) {
    throw new Error(data?.message || 'Đăng ký thất bại');
  }
  const { user, accessToken } = data.data;
  // User đã login thành công → cho phép restore session lần sau
  localStorage.removeItem('cll_skip_restore');
  useAuthStore.getState().setAuth(accessToken, user);
  return { user, accessToken };
}

/**
 * POST /api/auth/login
 * @param {{ email: string, password: string }}
 */
export async function login({ email, password }) {
  const { data } = await api.post('/auth/login', { email, password });
  if (!data?.success || !data?.data) {
    throw new Error(data?.message || 'Đăng nhập thất bại');
  }
  const { user, accessToken, expiresAt } = data.data;
  // User đã login thành công → cho phép restore session lần sau
  localStorage.removeItem('cll_skip_restore');
  useAuthStore.getState().setAuth(accessToken, user);
  return { user, accessToken, expiresAt };
}

/**
 * POST /api/auth/refresh
 * Gọi tự động bởi axios interceptor khi 401
 */
export async function refreshToken() {
  const { data } = await api.post('/auth/refresh');
  if (!data?.success || !data?.data?.accessToken) {
    throw new Error('Refresh token failed');
  }
  const { accessToken, user } = data.data;
  useAuthStore.getState().setAuth(accessToken, user);
  return { accessToken, user };
}

/**
 * POST /api/auth/logout
 */
export async function logout() {
  const token = useAuthStore.getState().accessToken;
  try {
    if (token) {
      await api.post('/auth/logout', { accessToken: token });
    }
  } finally {
    // Đánh dấu user đã chủ động logout trên máy này → không auto restore bằng refresh token nữa
    try {
      localStorage.setItem('cll_skip_restore', '1');
    } catch {
      // ignore
    }
    useAuthStore.getState().logout();
  }
}

/**
 * GET /api/users/profile
 */
export async function getProfile() {
  const { data } = await api.get('/users/profile');
  if (!data?.success) {
    throw new Error('Lấy thông tin thất bại');
  }
  useAuthStore.getState().updateUser(data.data);
  return data.data;
}

/**
 * PUT /api/users/profile
 */
export async function updateProfile(payload) {
  const { data } = await api.put('/users/profile', payload);
  if (!data?.success) {
    throw new Error('Cập nhật thất bại');
  }
  useAuthStore.getState().updateUser(data.data);
  return data.data;
}

/**
 * PUT /api/users/change-password
 */
export async function changePassword(payload) {
  const { data } = await api.put('/users/change-password', payload);
  if (!data?.success) {
    throw new Error('Đổi mật khẩu thất bại');
  }
  return data.data;
}
```

### 3.4. Axios Instance with Secure Interceptor

```javascript
// src/services/api.js
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { API_BASE_URL } from '@/utils/constants';

const REFRESH_PATH = '/auth/refresh';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Gửi cookie refreshToken
});

/** Gắn accessToken vào mỗi request */
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

/** 401 → refresh token → retry (trừ /auth/refresh và /auth/login) */
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const url = originalRequest?.url || '';

    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !url.includes(REFRESH_PATH) &&
      !url.includes('/auth/login')
    ) {
      originalRequest._retry = true;
      try {
        const { data } = await axios.post(API_BASE_URL + REFRESH_PATH, null, {
          withCredentials: true,
        });
        if (data?.success && data?.data?.accessToken) {
          useAuthStore.getState().setAuth(data.data.accessToken, data.data.user);
          originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`;
          return api(originalRequest);
        }
      } catch {
        useAuthStore.getState().logout();
        // Redirect to login page
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);
```

### 3.5. App Initialization - Session Restore

```javascript
// src/main.jsx or src/App.jsx
import { useEffect } from 'react';
import { useAuthStore } from '@/stores/authStore';

function App() {
  const tryRestoreSession = useAuthStore((state) => state.tryRestoreSession);

  useEffect(() => {
    // Thử restore session khi app load
    tryRestoreSession();
  }, [tryRestoreSession]);

  return (
    // Router and app components
  );
}
```

### 3.6. Protected Routes

```javascript
// src/components/common/ProtectedRoute.jsx
import { Navigate } from 'react-router-dom';
import { selectIsAuthenticated, selectIsAdmin } from '@/stores/authStore';

export function ProtectedRoute({ children, requireAdmin = false }) {
  const isAuthenticated = selectIsAuthenticated(useAuthStore.getState());
  const isAdmin = selectIsAdmin(useAuthStore.getState());

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/" />;
  }

  return <>{children}</>;
}
```

### 3.7. Security Best Practices

**Token Storage:**
- ✅ Access token: Memory (Zustand state) - cleared on page refresh
- ✅ Refresh token: HTTP-only cookie - inaccessible to JavaScript
- ❌ NEVER store access token in localStorage/sessionStorage
- ❌ NEVER store refresh token in JavaScript-accessible storage

**Token Transmission:**
- ✅ Access token: Authorization header (`Bearer <token>`)
- ✅ Refresh token: Automatic cookie (HttpOnly, Secure, SameSite=None)
- ✅ Always use HTTPS in production
- ✅ Cookie path restricted to `/api/auth/refresh`

**Session Management:**
- ✅ Multi-device support: Each device has independent session
- ✅ Logout only affects current device (refresh token rotation)
- ✅ Auto-restore session on page load (unless explicitly logged out)
- ✅ Prevent auto-restore after explicit logout using `cll_skip_restore` flag

**Error Handling:**
- ✅ 401 errors trigger automatic token refresh
- ✅ Refresh failures redirect to login page
- ✅ Network errors handled gracefully
- ✅ User-friendly error messages

**XSS Prevention:**
- ✅ Access token in memory - cannot be stolen by XSS
- ✅ Refresh token in HttpOnly cookie - cannot be accessed by JS
- ✅ Sanitize user input before rendering
- ✅ Use Content Security Policy (CSP) headers

**CSRF Prevention:**
- ✅ SameSite=None cookie attribute for cross-origin (Vercel ↔ Render)
- ✅ CSRF tokens not needed with HttpOnly cookies
- ✅ Verify origin/referrer headers on backend

### 3.8. Token Lifecycle Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    TOKEN LIFECYCLE                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. LOGIN                                                    │
│     ├─ User enters credentials                              │
│     ├─ POST /api/auth/login                                  │
│     ├─ Backend validates                                    │
│     ├─ Returns: accessToken + setCookie refreshToken        │
│     ├─ Frontend: Store AT in memory                         │
│     └─ Remove cll_skip_restore flag                          │
│                                                              │
│  2. AUTHENTICATED REQUEST                                   │
│     ├─ Add Authorization: Bearer <AT> header                 │
│     ├─ Send request with credentials (cookie)               │
│     └─ Backend validates JWT                                │
│                                                              │
│  3. TOKEN EXPIRED (401)                                     │
│     ├─ Axios interceptor catches 401                        │
│     ├─ POST /api/auth/refresh (cookie auto-sent)            │
│     ├─ Backend validates refresh token                       │
│     ├─ Returns: new AT + rotate refresh cookie              │
│     ├─ Update AT in memory                                  │
│     └─ Retry original request with new AT                    │
│                                                              │
│  4. REFRESH FAILED                                          │
│     ├─ Refresh token invalid/expired                        │
│     ├─ Clear AT from memory                                 │
│     └─ Redirect to /login                                    │
│                                                              │
│  5. LOGOUT (Current Device)                                 │
│     ├─ POST /api/auth/logout with AT                        │
│     ├─ Backend: Blacklist AT, delete refresh token          │
│     ├─ Frontend: Clear AT from memory                       │
│     ├─ Set cll_skip_restore flag                            │
│     └─ Clear refresh cookie (Max-Age=0)                     │
│                                                              │
│  6. PAGE REFRESH                                             │
│     ├─ Check cll_skip_restore flag                          │
│     ├─ If flag set → stay logged out                         │
│     ├─ If flag not set → tryRestoreSession()                │
│     ├─ POST /api/auth/refresh (cookie auto-sent)            │
│     ├─ If success → restore session                         │
│     └─ If fail → stay logged out                            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 3.9. Security Checklist

**Implementation Checklist:**
- [ ] Access token stored in memory (Zustand), NOT localStorage
- [ ] Refresh token handled as HTTP-only cookie
- [ ] Axios interceptor implements automatic token refresh
- [ ] Session restore on app load (unless explicitly logged out)
- [ ] Logout prevention flag (`cll_skip_restore`) implemented
- [ ] Protected routes check authentication before rendering
- [ ] Admin routes check role before rendering
- [ ] All API calls use the configured axios instance
- [ ] Error handling for 401, 403, 500 status codes
- [ ] User-friendly error messages displayed

**Production Checklist:**
- [ ] HTTPS enabled for all domains
- [ ] Cookie attributes: HttpOnly, Secure, SameSite=None
- [ ] Cookie path restricted to `/api/auth/refresh`
- [ ] CORS configured correctly (Vercel ↔ Render)
- [ ] Content Security Policy (CSP) headers configured
- [ ] JWT secrets are strong and environment-specific
- [ ] Redis for refresh token storage (not database)
- [ ] Rate limiting implemented on backend
- [ ] Monitoring for suspicious activity
- [ ] Regular security audits

---

## 4. Public Pages (không cần đăng nhập)

### 4.1. Trang chủ (`/`)

**API cần gọi:**

1. **Thống kê tổng quan**
   - `GET /api/public/stats`
   - Hiển thị: tổng văn bản, loại, cơ quan, ngày cập nhật

2. **Văn bản mới ban hành**
   - `GET /api/public/documents/recent?limit=10`
   - Card list với số hiệu, tiêu đề, ngày ban hành

3. **Văn bản nổi bật (xem nhiều)**
   - `GET /api/public/documents/popular?limit=10`
   - Card list với view count

4. **Danh sách danh mục**
   - `GET /api/public/categories`
   - Sidebar filter hoặc section

**UI Components:**
- `StatsCard` - Hiển thị thống kê
- `DocumentCard` - Card văn bản
- `SectionHeader` - Tiêu đề section

**Responsive Guidelines:**
- Stats grid: 1 column mobile, 2 columns tablet, 4 columns desktop
- Document cards: 1 column mobile, 2 columns tablet, 3 columns desktop
- Section headers: Responsive typography (h2 mobile, h1 desktop)

### 4.2. Trang tra cứu (`/search`)

**API cần gọi:**

1. **Tra cứu văn bản**
   - `GET /api/documents/search`
   - Query params: `q`, `documentNumber`, `type`, `issuingBody`, `year`, `status`, `categoryId`, `tagId`, `dateFrom`, `dateTo`, `page`, `size`, `sort`

2. **Autocomplete / Gợi ý**
   - `GET /api/documents/suggest?q={query}&limit=8`
   - Debounce 300ms

3. **Bộ lọc metadata**
   - `GET /api/documents/filters`
   - Cache response (5 phút)

**UI Components:**
- `SearchBar` - Input search với autocomplete
- `FilterPanel` - Panel filter đa điều kiện
- `DocumentList` - List kết quả với pagination
- `HighlightText` - Highlight từ khóa trong kết quả

**Responsive Guidelines:**
- Search bar: Full width mobile, fixed width desktop
- Filter panel: Collapsible on mobile (drawer/sheet), sidebar on desktop
- Document list: 1 column mobile, 2 columns tablet, 3 columns desktop
- Pagination: Show page numbers on desktop, simple prev/next on mobile

### 4.3. Chi tiết văn bản (`/documents/:id`)

**API cần gọi:**

1. **Lấy chi tiết văn bản**
   - `GET /api/documents/{id}?highlight={query}`
   - Tăng view count tự động

2. **Mục lục điều khoản**
   - `GET /api/documents/{id}/sections`
   - Tree view với anchor jump

3. **Quan hệ văn bản**
   - `GET /api/documents/{id}/relations`
   - Group theo loại quan hệ

4. **Kiểm tra bookmark status** (nếu đăng nhập)
   - `GET /api/bookmarks/{documentId}/status`

**UI Components:**
- `DocumentHeader` - Metadata văn bản
- `DocumentContent` - Nội dung văn bản HTML (hiển thị trực tiếp)
- `TableOfContents` - Mục lục tree view
- `RelationList` - Danh sách quan hệ
- `BookmarkButton` - Nút bookmark (nếu login)
- `ContentToggle` - Toggle giữa HTML và Text view (optional)

**Responsive Guidelines:**
- Document header: Stack layout mobile, horizontal desktop
- Table of contents: Collapsible drawer on mobile, sticky sidebar on desktop
- Document content: Full width with readable line-height (1.6) and max-width (800px)
  - HTML content: Render trực tiếp với sanitization
  - Responsive typography cho mobile (font-size 16px base)
- Relations: Vertical list mobile, grid desktop
- Action buttons: Bottom fixed bar on mobile, inline on desktop

**Lưu ý:**
- Không có tính năng tải PDF
- Hiển thị nội dung HTML trực tiếp từ `contentHtml` field
- Nếu `contentHtml` không có, fallback sang `contentText`

---

## 5. User Features (cần đăng nhập)

### 5.1. Hồ sơ cá nhân (`/profile`)

**API cần gọi:**

1. **Lấy profile**
   - `GET /api/users/profile`

2. **Cập nhật profile**
   - `PUT /api/users/profile`
   - Form: fullName

3. **Đổi mật khẩu**
   - `PUT /api/users/change-password`
   - Form: currentPassword, newPassword

**UI Components:**
- `ProfileForm` - Form cập nhật thông tin
- `ChangePasswordForm` - Form đổi mật khẩu

**Responsive Guidelines:**
- Profile form: Full width mobile, centered card (max-width 600px) desktop
- Form fields: Stack layout mobile, grid (2 columns) desktop where appropriate
- Submit button: Full width mobile, auto width desktop

### 5.2. Bookmark (`/bookmarks`)

**API cần gọi:**

1. **Danh sách bookmark**
   - `GET /api/bookmarks?page=0&size=20&sort=createdAt,desc`

2. **Thêm bookmark**
   - `POST /api/bookmarks/{documentId}`

3. **Xóa bookmark**
   - `DELETE /api/bookmarks/{documentId}`

4. **Kiểm tra trạng thái**
   - `GET /api/bookmarks/{documentId}/status`

**UI Components:**
- `BookmarkList` - List bookmark với pagination
- `BookmarkButton` - Toggle bookmark icon

**Responsive Guidelines:**
- Bookmark list: 1 column mobile, 2 columns tablet, 3 columns desktop
- Bookmark button: Icon-only on mobile, icon + text on desktop

### 5.3. Lịch sử tìm kiếm (`/history/search`)

**API cần gọi:**

1. **Danh sách lịch sử**
   - `GET /api/history/search?page=0&size=20`

2. **Xóa toàn bộ**
   - `DELETE /api/history/search`

**UI Components:**
- `SearchHistoryList` - List lịch sử
- `ClearHistoryButton` - Nút xóa lịch sử

**Responsive Guidelines:**
- History list: Compact list mobile, detailed cards desktop
- Clear button: Icon-only mobile, icon + text desktop

### 5.4. Lịch sử xem văn bản (`/history/views`)

**API cần gọi:**

1. **Danh sách lịch sử xem**
   - `GET /api/history/views?page=0&size=20`

2. **Xóa lịch sử xem**
   - `DELETE /api/history/views`

**UI Components:**
- `ViewHistoryList` - List lịch sử xem

**Responsive Guidelines:**
- View history list: Same as bookmark list (1/2/3 columns)
- Document preview: Truncate text on mobile, show full on desktop

### 5.5. AI Features

**API cần gọi:**

1. **Tóm tắt văn bản**
   - `POST /api/ai/documents/{id}/summarize`
   - Loading state, hiển thị disclaimer

2. **Hỏi đáp theo văn bản**
   - `POST /api/ai/documents/{id}/ask`
   - Form input question, hiển thị answer + sources

3. **Giải thích điều luật**
   - `POST /api/ai/documents/{id}/explain`
   - User select text, gửi context

4. **Kiểm tra quota**
   - `GET /api/ai/quota`
   - Hiển thị remaining/limit

**UI Components:**
- `AISummaryPanel` - Panel tóm tắt
- `AIChatPanel` - Chat interface hỏi đáp
- `AIExplainTooltip` - Tooltip giải thích khi select text
- `QuotaIndicator` - Hiển thị quota còn lại

**Responsive Guidelines:**
- AI panels: Full width mobile, max-width (800px) desktop
- Chat interface: Full screen mobile, centered panel desktop
- Quota indicator: Compact badge mobile, detailed card desktop
- Explain tooltip: Position-aware (bottom on mobile, right on desktop)

**Rate Limit Handling:**
- Hiển thị thông báo khi 429
- Hiển thị reset time

---

## 6. Admin Dashboard

### 6.1. Layout Admin

**Sidebar menu:**
- Dashboard (`/admin`)
- Quản lý văn bản (`/admin/documents`)
- Quan hệ văn bản (`/admin/relations`)
- Danh mục (`/admin/categories`)
- Tags (`/admin/tags`)
- Users (`/admin/users`)
- Crawl logs (`/admin/crawl`) - optional

**Responsive Guidelines:**
- Sidebar: Collapsible drawer on mobile, fixed sidebar on desktop
- Hamburger menu button on mobile to toggle sidebar
- Content area: Full width on mobile, with sidebar offset on desktop
- Tables: Horizontal scroll on mobile, full width on desktop

### 6.2. Dashboard (`/admin`)

**API:**
- `GET /api/admin/dashboard?period=7d`

**UI Components:**
- `StatsGrid` - Tổng quan (documents, users, searches, views, downloads, AI requests)
- `TopKeywordsChart` - Chart top keywords
- `TopDocumentsList` - List top documents
- `RecentActivityList` - Recent audit logs

**Responsive Guidelines:**
- Stats grid: 2 columns mobile, 3 columns tablet, 6 columns desktop
- Charts: Full width mobile, fixed height (300px) desktop
- Lists: Compact mobile, detailed desktop
- Activity feed: Vertical timeline mobile, horizontal cards desktop

### 6.3. Quản lý văn bản (`/admin/documents`)

**API cần gọi:**

1. **Danh sách văn bản**
   - `GET /api/admin/documents`
   - Query params: search, filters, includeDeleted, pagination

2. **Tạo văn bản**
   - `POST /api/admin/documents`
   - Form: documentNumber, title, abstract, documentType, issuingBody, signer, issuedDate, effectiveDate, expiryDate, status, contentText, contentHtml, sourceUrl, categoryIds, tagIds

3. **Cập nhật văn bản**
   - `PUT /api/admin/documents/{id}`
   - Form giống tạo

4. **Xóa (soft delete)**
   - `DELETE /api/admin/documents/{id}`

5. **Khôi phục**
   - `POST /api/admin/documents/{id}/restore`

6. **Upload HTML content**
   - Không có upload PDF
   - Nhập HTML content trực tiếp trong form hoặc paste từ nguồn

**UI Components:**
- `DocumentTable` - Table với actions (edit, delete, restore)
- `DocumentForm` - Form tạo/sửa (multi-step hoặc tabs)
- `HtmlContentEditor` - Editor cho HTML content (textarea hoặc rich text editor)
- `DeleteConfirmDialog` - Dialog xác nhận xóa

**Responsive Guidelines:**
- Document table: Horizontal scroll on mobile, full width on desktop
- Table actions: Dropdown menu on mobile, inline buttons on desktop
- Document form: Full width mobile, multi-column grid desktop
- HTML content editor: Full height textarea mobile, fixed height desktop
- Dialog: Full screen mobile, centered modal desktop

### 6.4. Quan hệ văn bản (`/admin/relations`)

**API cần gọi:**

1. **Danh sách quan hệ của văn bản**
   - `GET /api/admin/documents/{id}/relations`

2. **Tạo quan hệ**
   - `POST /api/admin/relations`
   - Form: sourceDocumentId, targetDocumentId, relationType, note

3. **Xóa quan hệ**
   - `DELETE /api/admin/relations/{relationId}`

**UI Components:**
- `RelationList` - List quan hệ
- `RelationForm` - Form tạo quan hệ với autocomplete document

**Responsive Guidelines:**
- Relation list: Vertical stack mobile, grid desktop
- Relation form: Full width mobile, 2-column grid desktop
- Autocomplete: Full width mobile, fixed width desktop

### 6.5. Danh mục & Tags

**API cần gọi:**

1. **CRUD Categories**
   - `GET /api/admin/categories`
   - `POST /api/admin/categories`
   - `PUT /api/admin/categories/{id}`
   - `DELETE /api/admin/categories/{id}`

2. **CRUD Tags**
   - `GET /api/admin/tags`
   - `POST /api/admin/tags`
   - `PUT /api/admin/tags/{id}`
   - `DELETE /api/admin/tags/{id}`

**UI Components:**
- `CategoryTable` - Danh sách danh mục
- `CategoryForm` - Form tạo/sửa
- `TagTable` - Danh sách tags
- `TagForm` - Form tạo/sửa

**Responsive Guidelines:**
- Category/Tag tables: Horizontal scroll mobile, full width desktop
- Forms: Full width mobile, centered card (max-width 600px) desktop
- Drag-and-drop for ordering: Touch-friendly on mobile, mouse on desktop

### 6.6. Quản lý Users (`/admin/users`)

**API cần gọi:**

1. **Danh sách users**
   - `GET /api/admin/users`
   - Query params: q, enabled, role, pagination

2. **Khóa user**
   - `POST /api/admin/users/{id}/disable`

3. **Mở khóa user**
   - `POST /api/admin/users/{id}/enable`

4. **Đổi role**
   - `PATCH /api/admin/users/{id}/role`

**UI Components:**
- `UserTable` - Table users với actions
- `UserActions` - Dropdown menu actions (disable/enable/change role)

**Responsive Guidelines:**
- User table: Horizontal scroll mobile, full width desktop
- User actions: Dropdown menu on mobile, inline buttons on desktop
- Role badge: Compact on mobile, full label on desktop

### 6.7. Crawl Logs (`/admin/crawl`) - Optional

**API cần gọi:**

1. **Trigger đồng bộ**
   - `POST /api/admin/crawl/sync`
   - Body: `{ mode: "full" | "incremental", source: "all" | "..." }`

2. **Danh sách logs**
   - `GET /api/admin/crawl/logs`

3. **Chi tiết log**
   - `GET /api/admin/crawl/logs/{id}`

**UI Components:**
- `CrawlTriggerButton` - Button trigger crawl
- `CrawlLogsTable` - Table logs
- `CrawlLogDetail` - Chi tiết log với error details

**Responsive Guidelines:**
- Crawl logs table: Horizontal scroll mobile, full width desktop
- Log detail: Full width mobile, centered card desktop
- Error details: Collapsible accordion mobile, expanded panel desktop

---

## 7. Components & UI Patterns

### 7.1. Custom Components (CSS Modules)

Tất cả components sử dụng CSS Modules với BEM naming convention:

**Base Components:**
- `Button` - Button variants (primary, secondary, ghost, danger)
- `Input` - Text input với validation states
- `Select` - Dropdown select
- `Textarea` - Multi-line text input
- `Modal` - Modal dialog
- `Dropdown` - Dropdown menu
- `Table` - Data table với pagination
- `Card` - Card container
- `Badge` - Status badge
- `Avatar` - User avatar
- `Form` - Form wrapper với validation
- `Label` - Form label
- `Toast` - Toast notifications
- `Skeleton` - Loading skeleton
- `Spinner` - Loading spinner

**CSS Module Example:**
```css
/* src/components/common/Button/Button.module.css */
.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-weight: 500;
  transition: all 0.2s;
  cursor: pointer;
  border: none;
}

.button--primary {
  background-color: var(--color-primary);
  color: white;
}

.button--primary:hover {
  background-color: var(--color-primary-dark);
}

.button--secondary {
  background-color: var(--color-secondary);
  color: var(--color-text-primary);
}

.button--secondary:hover {
  background-color: var(--color-secondary-dark);
}

.button--danger {
  background-color: var(--color-danger);
  color: white;
}

.button--danger:hover {
  background-color: var(--color-danger-dark);
}

/* Responsive */
@media (max-width: 640px) {
  .button {
    width: 100%;
  }
}
```

### 7.2. Domain Components

**DocumentCard:**
```javascript
// src/components/document/DocumentCard.jsx
import styles from './DocumentCard.module.css';

export function DocumentCard({ document, showBookmark = false, bookmarked = false, onBookmarkToggle }) {
  return (
    <article className={styles.card}>
      <div className={styles.card__header}>
        <span className={styles.badge}>{document.documentType}</span>
        {showBookmark && (
          <button
            className={`${styles.bookmarkButton} ${bookmarked ? styles.bookmarkButton--active : ''}`}
            onClick={onBookmarkToggle}
            aria-label={bookmarked ? 'Xóa khỏi yêu thích' : 'Thêm vào yêu thích'}
          >
            <BookmarkIcon filled={bookmarked} />
          </button>
        )}
      </div>
      <h3 className={styles.card__title}>{document.title}</h3>
      <p className={styles.card__number}>{document.documentNumber}</p>
      <div className={styles.card__meta}>
        <span>{document.issuingBody}</span>
        <span>{formatDate(document.issuedDate)}</span>
      </div>
    </article>
  );
}
```

**SearchBar:**
```javascript
// src/components/search/SearchBar.jsx
import styles from './SearchBar.module.css';

export function SearchBar({ onSearch, placeholder = 'Tìm kiếm văn bản...', showFilters = true }) {
  const [query, setQuery] = useState('');
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [suggestions, setSuggestions] = useState([]);

  const debouncedQuery = useDebounce(query, 300);

  useEffect(() => {
    if (debouncedQuery.length >= 2) {
      fetchSuggestions(debouncedQuery);
    }
  }, [debouncedQuery]);

  const fetchSuggestions = async (q) => {
    const { data } = await api.get('/documents/suggest', { params: { q, limit: 8 } });
    setSuggestions(data.data);
    setShowSuggestions(true);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch(query);
    setShowSuggestions(false);
  };

  return (
    <form className={styles.searchBar} onSubmit={handleSubmit}>
      <div className={styles.searchBar__inputWrapper}>
        <SearchIcon className={styles.searchBar__icon} />
        <input
          type="text"
          className={styles.searchBar__input}
          placeholder={placeholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setShowSuggestions(true)}
        />
        {showFilters && (
          <button
            type="button"
            className={styles.searchBar__filterButton}
            aria-label="Bộ lọc"
          >
            <FilterIcon />
          </button>
        )}
      </div>
      {showSuggestions && suggestions.length > 0 && (
        <ul className={styles.searchBar__suggestions}>
          {suggestions.map((doc) => (
            <li key={doc.id} className={styles.searchBar__suggestion}>
              <button
                type="button"
                onClick={() => {
                  setQuery(doc.documentNumber);
                  onSearch(doc.documentNumber);
                  setShowSuggestions(false);
                }}
              >
                <span className={styles.searchBar__suggestionNumber}>{doc.documentNumber}</span>
                <span className={styles.searchBar__suggestionTitle}>{doc.title}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </form>
  );
}
```

**FilterPanel:**
```javascript
// src/components/search/FilterPanel.jsx
import styles from './FilterPanel.module.css';

export function FilterPanel({ filters, onFilterChange, availableFilters }) {
  return (
    <aside className={styles.filterPanel}>
      <div className={styles.filterPanel__header}>
        <h2 className={styles.filterPanel__title}>Bộ lọc</h2>
        <button
          className={styles.filterPanel__resetButton}
          onClick={() => onFilterChange({})}
        >
          Đặt lại
        </button>
      </div>

      <div className={styles.filterPanel__section}>
        <label className={styles.filterPanel__label}>Loại văn bản</label>
        <select
          className={styles.filterPanel__select}
          value={filters.type || ''}
          onChange={(e) => onFilterChange({ ...filters, type: e.target.value })}
        >
          <option value="">Tất cả</option>
          {availableFilters.documentTypes.map((type) => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
      </div>

      <div className={styles.filterPanel__section}>
        <label className={styles.filterPanel__label}>Trạng thái</label>
        <select
          className={styles.filterPanel__select}
          value={filters.status || ''}
          onChange={(e) => onFilterChange({ ...filters, status: e.target.value })}
        >
          <option value="">Tất cả</option>
          {availableFilters.statuses.map((status) => (
            <option key={status.value} value={status.value}>
              {status.label}
            </option>
          ))}
        </select>
      </div>

      {/* Additional filters... */}
    </aside>
  );
}
```

### 7.3. Loading States

- Skeleton loader cho list/table
- Spinner cho button/action
- Progress bar cho upload

**Responsive Guidelines:**
- Skeleton: Match layout of actual content (1/2/3 columns)
- Spinner: Centered overlay on mobile, inline on desktop
- Progress bar: Full width mobile, fixed width desktop

### 7.4. Empty States

- Empty state cho search results
- Empty state cho bookmarks/history
- CTA để user thực hiện action

**Responsive Guidelines:**
- Empty state illustration: Responsive size (smaller on mobile)
- CTA button: Full width mobile, auto width desktop
- Message text: Shorter on mobile, detailed on desktop

---

## 8. State Management

### 8.1. Document Store (Zustand)

```javascript
// src/stores/documentStore.js
import { create } from 'zustand';

export const useDocumentStore = create((set, get) => ({
  filters: {},
  results: [],
  total: 0,
  page: 0,
  loading: false,

  setFilters: (filters) => set({ filters, page: 0 }),

  setPage: (page) => set({ page }),

  search: async () => {
    const { filters, page } = get();
    set({ loading: true });

    try {
      const { data } = await api.get('/documents/search', {
        params: { ...filters, page, size: 20 }
      });

      set({
        results: data.data.content,
        total: data.data.totalElements,
        loading: false
      });
    } catch (error) {
      set({ loading: false });
      throw error;
    }
  },
}));
```

### 8.2. UI Store (Zustand)

```javascript
// src/stores/uiStore.js
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useUIStore = create(
  persist(
    (set) => ({
      sidebarOpen: false,
      theme: 'light',

      toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
      closeSidebar: () => set({ sidebarOpen: false }),
      setTheme: (theme) => set({ theme }),
    }),
    {
      name: 'ui-storage',
    }
  )
);
```

### 8.3. Local Storage

Sử dụng Zustand persist middleware cho state persistence:
- Auth store: Persist user info (không persist accessToken vì lưu riêng)
- UI store: Persist theme, sidebar state
- Manual: `accessToken` lưu trực tiếp trong localStorage

---

## 9. Error Handling

### 9.1. Error Types

| HTTP Status | Xử lý |
|-------------|-------|
| 400 | Validation error - hiển thị field errors |
| 401 | Unauthorized - redirect login |
| 403 | Forbidden - hiển thị message không có quyền |
| 404 | Not found - hiển thị page not found |
| 409 | Conflict - hiển thị message (trùng email, bookmark) |
| 429 | Rate limit - hiển thị message AI quota exceeded |
| 500 | Server error - hiển thị generic error message |

### 9.2. Toast Notifications

Sử dụng react-hot-toast hoặc custom toast implementation:

```javascript
// src/utils/toast.js
import toast from 'react-hot-toast';

export const showToast = {
  success: (message) => toast.success(message),
  error: (message) => toast.error(message),
  loading: (message) => toast.loading(message),
  promise: (promise, messages) => toast.promise(promise, messages),
};

// Usage
showToast.success('Đăng nhập thành công');
showToast.error('Đăng nhập thất bại');
showToast.promise(loginPromise, {
  loading: 'Đang đăng nhập...',
  success: 'Đăng nhập thành công',
  error: 'Đăng nhập thất bại',
});
```

**Responsive Guidelines:**
- Toast position: Bottom-center on mobile, top-right on desktop
- Toast width: Full width mobile (90%), fixed width desktop (400px)
- Toast animation: Slide up on mobile, fade in on desktop

### 9.3. Error Boundary

```javascript
// src/components/common/ErrorBoundary.jsx
import { Component } from 'react';

export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="errorBoundary">
          <h1>Đã có lỗi xảy ra</h1>
          <p>Vui lòng tải lại trang hoặc liên hệ hỗ trợ.</p>
          <button onClick={() => window.location.reload()}>Tải lại</button>
        </div>
      );
    }

    return this.props.children;
  }
}
```

---

## 10. Performance & Optimization

### 10.1. Code Splitting

```javascript
// Lazy load routes
import { lazy, Suspense } from 'react';

const AdminDashboard = lazy(() => import('./pages/admin/Dashboard'));
const DocumentDetail = lazy(() => import('./pages/public/DocumentDetail'));

// Wrap with Suspense
<Suspense fallback={<Spinner />}>
  <AdminDashboard />
</Suspense>
```

### 10.2. API Caching

- Cache filters metadata (5 phút)
- Cache categories/tags (10 phút)
- Cache public stats (5 phút)

### 10.3. Debounce & Throttle

```javascript
// src/hooks/useDebounce.js
import { useState, useEffect } from 'react';

export function useDebounce(value, delay = 300) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}
```

- Debounce search input (300ms)
- Throttle scroll events

### 10.4. Pagination

- Default page size: 20
- Max page size: 100
- Infinite scroll hoặc traditional pagination

**Responsive Guidelines:**
- Pagination controls: Show page numbers on desktop, simple prev/next on mobile
- Page size selector: Dropdown on desktop, select on mobile
- Jump to page: Input field on desktop, hidden on mobile

### 10.5. Image Optimization

- Lazy load images
- WebP format với fallback
- Responsive images với srcset

**Responsive Guidelines:**
- Image sizes: Serve smaller images for mobile (< 640px)
- Lazy load: Intersection Observer for mobile, eager load for above-fold desktop

---

## Phụ lục A - Checklist Implement

### Public Pages
- [ ] Trang chủ với thống kê, recent, popular documents
- [ ] Trang search với filters và pagination
- [ ] Trang chi tiết văn bản với sections, relations
- [ ] Autocomplete search
- [ ] Hiển thị nội dung HTML (không có PDF download)

### Auth
- [ ] Đăng ký
- [ ] Đăng nhập
- [ ] Đăng xuất
- [ ] Refresh token tự động
- [ ] Protected routes

### User Features
- [ ] Profile (view, update)
- [ ] Đổi mật khẩu
- [ ] Bookmarks (list, add, remove, status)
- [ ] Lịch sử tìm kiếm
- [ ] Lịch sử xem
- [ ] AI tóm tắt
- [ ] AI hỏi đáp
- [ ] AI giải thích
- [ ] AI quota indicator

### Admin Dashboard
- [ ] Dashboard với thống kê
- [ ] Quản lý văn bản (CRUD, soft delete, restore, upload PDF)
- [ ] Quản lý quan hệ văn bản
- [ ] Quản lý danh mục
- [ ] Quản lý tags
- [ ] Quản lý users (disable/enable/change role)
- [ ] Crawl logs (optional)

---

## Phụ lục B - Liên kết tài liệu

| Tài liệu | Mô tả |
|----------|--------|
| [API.md](./API.md) | REST API documentation |
| [DATABASE.md](./DATABASE.md) | Database schema & design |
| Swagger UI | `/swagger-ui.html` (khi backend chạy) |

---

*Tài liệu này là baseline cho phát triển frontend. Cập nhật version khi có thay đổi API hoặc yêu cầu mới.*

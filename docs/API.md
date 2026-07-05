# API — Construction Legal Lookup

> Tài liệu REST API cho hệ thống tra cứu văn bản pháp luật ngành xây dựng.  
> **Backend:** Spring Boot 3 · **Auth:** JWT + Redis Refresh Token · **Docs live:** Swagger UI  
> **Phiên bản:** 1.1 · **Cập nhật:** 2026-07-03 · **Base path:** `/api`  
> **Auth pattern:** JWT Access + Refresh Cookie (Redis), role-only — tham khảo `API-example.md`, đơn giản hóa (không permission, không email verification).

---

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Quy ước chung](#2-quy-ước-chung)
3. [Authentication & Authorization](#3-authentication--authorization)
4. [Định dạng Response & Lỗi](#4-định-dạng-response--lỗi)
5. [Auth API](#5-auth-api)
6. [Public — Trang chủ & Thống kê](#6-public--trang-chủ--thống-kê)
7. [Documents API](#7-documents-api)
8. [Bookmarks API](#8-bookmarks-api)
9. [Lịch sử người dùng API](#9-lịch-sử-người-dùng-api)
10. [Hồ sơ cá nhân API](#10-hồ-sơ-cá-nhân-api)
11. [AI API](#11-ai-api)
12. [Admin — Dashboard](#12-admin--dashboard)
13. [Admin — Quản lý văn bản](#13-admin--quản-lý-văn-bản)
14. [Admin — Danh mục & Tag](#14-admin--danh-mục--tag)
15. [Admin — Quản lý User](#15-admin--quản-lý-user)
16. [Admin — Crawl & Nhật ký đồng bộ](#16-admin--crawl--nhật-ký-đồng-bộ)
17. [Health & Metadata](#17-health--metadata)
18. [Enum tham chiếu](#18-enum-tham-chiếu)
19. [Ma trận phân quyền](#19-ma-trận-phân-quyền)

---

## 1. Tổng quan

### 1.1. Base URL

| Môi trường | URL |
|------------|-----|
| Local | `http://localhost:8080/api` |
| Production | `https://{app-name}.onrender.com/api` |

Frontend (Vercel) gọi API qua biến `VITE_API_URL`.

### 1.2. Kiến trúc API

```
Client (React)
    │
    ├─► Access Token  ──► Authorization: Bearer {jwt}        (header, ~15 phút)
    │
    └─► Refresh Token ──► Cookie: refreshToken=...             (httpOnly, ~7 ngày, Redis)
                              Path=/api/auth/refresh
```

- **Guest** được phép: tra cứu, xem chi tiết, autocomplete, trang chủ.
- **USER** (đăng nhập): bookmark, lịch sử, AI, hồ sơ.
- **ADMIN**: toàn bộ endpoint `/api/admin/**` — kiểm tra **role** trong JWT, **không dùng permission**.
- **Refresh token** không trả trong body — chỉ set qua **HTTP Cookie** (chống XSS).

### 1.3. Content-Type mặc định

| Loại request | Header |
|--------------|--------|
| JSON | `Content-Type: application/json` |
| Upload file | `Content-Type: multipart/form-data` |

Response luôn là `application/json` (trừ download PDF stream).

---

## 2. Quy ước chung

### 2.1. HTTP Methods

| Method | Dùng cho |
|--------|----------|
| `GET` | Đọc dữ liệu |
| `POST` | Tạo mới / hành động (login, AI, bookmark) |
| `PUT` | Cập nhật toàn phần |
| `PATCH` | Cập nhật một phần |
| `DELETE` | Xóa (soft delete với document/user) |

### 2.2. Phân trang

Query params chuẩn Spring Data:

| Param | Kiểu | Mặc định | Mô tả |
|-------|------|----------|-------|
| `page` | int | `0` | Trang (0-based) |
| `size` | int | `20` | Số phần tử/trang (max 100) |
| `sort` | string | tùy endpoint | VD: `issuedDate,desc` |

**Cấu trúc `Page` trong response:**

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

### 2.3. Thời gian

- Format ISO-8601: `2026-07-03T14:30:00+07:00`
- Date only: `2026-07-03`

### 2.4. ID

- Kiểu `number` (BIGINT), ví dụ: `42`

---

## 3. Authentication & Authorization

> Thiết kế auth tham khảo pattern **JWT Access + Refresh Cookie (Redis)** — **đơn giản hóa**: chỉ phân quyền theo **role** (`USER` / `ADMIN`), **không có** hệ thống permission.

### 3.1. Access Token (JWT)

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Payload mẫu:**

```json
{
  "sub": "1",
  "email": "user@example.com",
  "role": "USER",
  "jti": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "iat": 1710000000,
  "exp": 1710000900
}
```

| Claim | Mô tả |
|-------|--------|
| `sub` | User ID |
| `email` | Email đăng nhập |
| `role` | `USER` hoặc `ADMIN` — backend `@PreAuthorize("hasRole('ADMIN')")` |
| `jti` | JWT ID — dùng khi logout (blacklist access token ngắn hạn) |
| `iat` | Issued at (Unix timestamp) |
| `exp` | Expiration (~15 phút) |

> **Không có** `permissions[]` trong JWT. Admin API chỉ check `role === ADMIN`.

### 3.2. Refresh Token (Cookie + Redis)

| Thuộc tính | Giá trị |
|------------|---------|
| Cookie name | `refreshToken` |
| Lưu trữ | Redis key `refresh_token:{jti}` |
| Body response | **Không trả** refresh token — chỉ set cookie |
| TTL Redis | 7 ngày (cấu hình được) |

**Redis value (`refresh_token:{jti}`):**

```json
{
  "userId": 1,
  "email": "user@example.com",
  "userAgent": "Mozilla/5.0...",
  "createdAt": "2026-07-03T14:30:00+07:00"
}
```

**Cookie attributes:**

| Tham số | Giá trị | Ý nghĩa |
|---------|---------|---------|
| **HttpOnly** | — | JS không đọc được — chống XSS |
| **Secure** | — | Chỉ gửi qua HTTPS |
| **SameSite** | `None` | Cross-origin Vercel ↔ Render (local dev có thể `Lax`) |
| **Path** | `/api/auth/refresh` | Cookie chỉ gửi khi gọi refresh — giảm phạm vi |
| **Max-Age** | `604800` | 7 ngày |

**Set-Cookie mẫu (login / refresh):**

```
Set-Cookie: refreshToken=<jti_or_token>; HttpOnly; Secure; SameSite=None; Path=/api/auth/refresh; Max-Age=604800
```

### 3.3. Multi-device

- Mỗi lần **login** tạo **session mới** (jti mới) — user có thể đăng nhập đồng thời nhiều thiết bị.
- **Logout** chỉ invalidate session **thiết bị hiện tại** (refresh token trong cookie).
- **Refresh** chỉ rotate token của device gọi API — không ảnh hưởng device khác.

### 3.4. Luồng Refresh (Axios interceptor)

```
1. GET/POST ... → 401 Unauthorized
2. POST /api/auth/refresh  (cookie refreshToken tự gửi — Path khớp)
3. Nhận accessToken + expiresAt mới → retry request gốc
4. Refresh fail → xóa accessToken local → redirect /login
```

### 3.5. Thu hồi token

| Sự kiện | Access token | Refresh token (Redis) |
|---------|--------------|------------------------|
| **Logout** (device hiện tại) | Blacklist `jti` (optional, TTL = thời gian còn lại) | `DEL refresh_token:{jti}` + clear cookie |
| **Refresh** | Cấp token mới | Rotate: xóa cũ, tạo mới |
| **Đổi mật khẩu** | — | Revoke **tất cả** session của user |
| **Admin khóa user** | — | Revoke **tất cả** session + `enabled = false` |

### 3.6. Phân quyền (role-only)

| Role | Quyền |
|------|-------|
| Guest | Public endpoints (search, xem VB, trang chủ) |
| `USER` | Guest + bookmark, lịch sử, AI, profile |
| `ADMIN` | USER + `/api/admin/**` |

Không có bảng `permissions`, không gán permission cho role.

---

## 4. Định dạng Response & Lỗi

### 4.1. Response thành công (đơn lẻ)

```json
{
  "success": true,
  "message": "Thao tác thành công",
  "data": { },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

> **Auth APIs** luôn có field `message` mô tả kết quả (VD: `"Login successful"`, `"Token refreshed successfully"`). Các API khác có thể bỏ qua `message` nếu `data` đủ rõ.

### 4.2. Response lỗi

**Validation (400):**

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "errors": [
    { "field": "email", "message": "Email không đúng định dạng" }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

**Lỗi nghiệp vụ / auth (401, 403, 404, 409...):**

```json
{
  "success": false,
  "message": "Email hoặc mật khẩu không đúng",
  "errors": [
    { "field": "authorization", "message": "JWT token is missing or invalid" }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

> Có thể bổ sung `errorCode` (VD: `UNAUTHORIZED`, `CONFLICT`) ở backend nếu frontend cần switch-case — không map theo permission.

### 4.3. Mã lỗi HTTP & `error.code`

| HTTP | `error.code` | Mô tả |
|------|--------------|-------|
| 400 | `VALIDATION_ERROR` | Dữ liệu đầu vào sai |
| 401 | `UNAUTHORIZED` | Chưa đăng nhập / token hết hạn |
| 403 | `FORBIDDEN` | Không đủ quyền |
| 404 | `NOT_FOUND` | Không tìm thấy tài nguyên |
| 409 | `CONFLICT` | Trùng email, trùng số hiệu văn bản |
| 429 | `RATE_LIMIT_EXCEEDED` | Vượt giới hạn AI / request |
| 500 | `INTERNAL_ERROR` | Lỗi server |

---

## 5. Auth API

> **Flow đơn giản** (không email verification, không forgot password, không permission):  
> Register → auto login · Login · Refresh · Logout

---

### 5.1. Đăng ký

**POST** `/api/auth/register`

Tạo tài khoản mới (`role = USER`), tự động đăng nhập — trả access token và set refresh cookie.

| | |
|---|---|
| **Auth** | Không |

#### Request Body

```json
{
  "email": "user@example.com",
  "password": "Demo@123",
  "fullName": "Nguyễn Văn A"
}
```

| Field | Kiểu | Bắt buộc | Validation |
|-------|------|----------|------------|
| `email` | string | ✅ | Email hợp lệ, unique |
| `password` | string | ✅ | Min 8 ký tự, có chữ hoa + số + ký tự đặc biệt |
| `fullName` | string | ❌ | Max 150 ký tự |

#### Response `201 Created`

```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "USER",
      "enabled": true
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2026-07-03T14:45:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

#### Response Headers

```
Set-Cookie: refreshToken=<token>; HttpOnly; Secure; SameSite=None; Path=/api/auth/refresh; Max-Age=604800
```

> - `accessToken` dùng header `Authorization: Bearer <accessToken>`
> - `refreshToken` **chỉ** qua cookie — không có trong body
> - `expiresAt`: thời điểm access token hết hạn (ISO 8601)

#### Error Response `409 Conflict`

```json
{
  "success": false,
  "message": "Email đã được sử dụng",
  "errors": [
    { "field": "email", "message": "Email already exists" }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 5.2. Đăng nhập

**POST** `/api/auth/login`

Đăng nhập email + password. Mỗi lần login tạo session mới (multi-device).

| | |
|---|---|
| **Auth** | Không |

#### Request Body

```json
{
  "email": "user@example.com",
  "password": "Demo@123"
}
```

#### Response `200 OK`

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "USER",
      "enabled": true
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2026-07-03T14:45:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

#### Response Headers

```
Set-Cookie: refreshToken=<token>; HttpOnly; Secure; SameSite=None; Path=/api/auth/refresh; Max-Age=604800
```

#### Error Response `401 Unauthorized`

```json
{
  "success": false,
  "message": "Email hoặc mật khẩu không đúng",
  "errors": [
    { "field": "credentials", "message": "Invalid email or password" }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

#### Error Response `403 Forbidden`

```json
{
  "success": false,
  "message": "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.",
  "errors": [
    { "field": "account", "message": "Account is disabled" }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 5.3. Refresh Access Token

**POST** `/api/auth/refresh`

Làm mới access token. Refresh token gửi qua **HTTP Cookie** (browser tự gửi khi request tới `Path=/api/auth/refresh`).

| | |
|---|---|
| **Auth** | Cookie `refreshToken` |

> **Multi-device**: Mỗi device có refresh token riêng trong Redis. Refresh chỉ rotate token device hiện tại.

#### Request

- **Cookie:** `refreshToken` (tự động)
- **Body:** Không

#### Response `200 OK`

```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "USER",
      "enabled": true
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2026-07-03T15:00:00+07:00"
  },
  "timestamp": "2026-07-03T14:45:00+07:00"
}
```

#### Response Headers

```
Set-Cookie: refreshToken=<new_token>; HttpOnly; Secure; SameSite=None; Path=/api/auth/refresh; Max-Age=604800
```

#### Error Response `401 Unauthorized`

```json
{
  "success": false,
  "message": "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
  "errors": [
    { "field": "refreshToken", "message": "Refresh token is missing or invalid" }
  ],
  "timestamp": "2026-07-03T14:45:00+07:00"
}
```

---

### 5.4. Đăng xuất

**POST** `/api/auth/logout`

Đăng xuất **chỉ thiết bị hiện tại** — invalidate access + refresh token của session này.

| | |
|---|---|
| **Auth** | Bearer ✅ + Cookie `refreshToken` |

**Token Invalidation Flow:**

1. Parse `accessToken` từ body → blacklist `jti` (TTL = thời gian access còn lại)
2. Parse `refreshToken` từ cookie → `DEL refresh_token:{jti}` khỏi Redis
3. Response set cookie `Max-Age=0` để browser xóa cookie

#### Request Headers

```
Authorization: Bearer <access_token>
Cookie: refreshToken=... (tự động gửi bởi browser)
```

#### Request Body

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Response `200 OK`

```json
{
  "success": true,
  "message": "Logout successful",
  "data": null,
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

#### Response Headers

```
Set-Cookie: refreshToken=; Max-Age=0; Path=/api/auth/refresh; HttpOnly; Secure; SameSite=None
```

> Các thiết bị khác vẫn đăng nhập bình thường (multi-device).

---

## 6. Public — Trang chủ & Thống kê

### 6.1. Thống kê tổng quan

**Mô tả:** Số liệu hiển thị trang chủ (tổng văn bản, loại, cơ quan ban hành).

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/public/stats` |
| **Auth** | Không |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "totalDocuments": 128,
    "totalTypes": 6,
    "totalIssuingBodies": 15,
    "datasetUpdatedAt": "2026-06-15T00:00:00+07:00",
    "datasetLabel": "Dữ liệu cập nhật đến 15/06/2026"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 6.2. Văn bản mới ban hành

**Mô tả:** Danh sách văn bản sort theo `issuedDate` giảm dần.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/public/documents/recent` |
| **Auth** | Không |

**Query Params:**

| Param | Kiểu | Mặc định |
|-------|------|----------|
| `limit` | int | `10` (max 20) |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": [
    {
      "id": 42,
      "documentNumber": "15/2022/NĐ-CP",
      "title": "Nghị định quy định chi tiết Luật Xây dựng",
      "documentType": "NGHI_DINH",
      "issuingBody": "Chính phủ",
      "issuedDate": "2022-03-31",
      "status": "CON_HIEU_LUC",
      "abstract": "Quy định chi tiết một số điều của Luật Xây dựng..."
    }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 6.3. Văn bản nổi bật (xem nhiều)

**Mô tả:** Sort theo `viewCount` giảm dần.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/public/documents/popular` |
| **Auth** | Không |

**Query Params:** `limit` (default 10)

**Response `200 OK`:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "documentNumber": "50/2014/QH13",
      "title": "Luật Xây dựng",
      "documentType": "LUAT",
      "viewCount": 1520,
      "issuedDate": "2014-06-18",
      "status": "HET_HIEU_LUC"
    }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 6.4. Văn bản mới cập nhật

**Mô tả:** Sort theo `updatedAt` — phản ánh lần import/admin sửa gần nhất.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/public/documents/updated` |
| **Auth** | Không |

**Query Params:** `limit` (default 10)

**Response `200 OK`:** Cùng cấu trúc mảng như [6.2](#62-văn-bản-mới-ban-hành), thêm field `updatedAt`.

---

### 6.5. Danh sách danh mục (public)

**Mô tả:** Categories hiển thị trang chủ / sidebar filter.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/public/categories` |
| **Auth** | Không |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Giấy phép xây dựng",
      "slug": "giay-phep-xay-dung",
      "documentCount": 24
    }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

## 7. Documents API

### 7.1. Tra cứu văn bản

**Mô tả:** Tìm kiếm đa điều kiện kết hợp full-text, filter, phân trang. Ghi audit `SEARCH` (và `search_history` nếu user đăng nhập).

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/documents/search` |
| **Auth** | Không (guest OK) |

**Query Params:**

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `q` | string | Từ khóa full-text |
| `documentNumber` | string | Tìm theo số hiệu (exact/prefix) |
| `type` | string | `document_type` enum |
| `issuingBody` | string | Cơ quan ban hành |
| `year` | int | Năm ban hành |
| `status` | string | Trạng thái hiệu lực |
| `categoryId` | long | Lọc theo danh mục |
| `tagId` | long | Lọc theo tag |
| `dateFrom` | date | Ngày ban hành từ |
| `dateTo` | date | Ngày ban hành đến |
| `page` | int | Trang (default 0) |
| `size` | int | Kích thước (default 20) |
| `sort` | string | `relevance` \| `issuedDate,desc` \| `viewCount,desc` |

**Ví dụ request:**

```
GET /api/documents/search?q=giấy+phép+xây+dựng&type=NGHI_DINH&status=CON_HIEU_LUC&page=0&size=20&sort=relevance
```

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 42,
        "documentNumber": "15/2022/NĐ-CP",
        "title": "Nghị định quy định chi tiết Luật Xây dựng",
        "abstract": "Quy định chi tiết một số điều của Luật Xây dựng...",
        "documentType": "NGHI_DINH",
        "issuingBody": "Chính phủ",
        "issuedDate": "2022-03-31",
        "effectiveDate": "2022-05-15",
        "status": "CON_HIEU_LUC",
        "viewCount": 890,
        "categories": [
          { "id": 1, "name": "Giấy phép xây dựng", "slug": "giay-phep-xay-dung" }
        ],
        "highlight": {
          "title": "Nghị định quy định chi tiết Luật Xây dựng",
          "abstract": "...cấp <mark>giấy phép xây dựng</mark>..."
        },
        "relevanceScore": 12.5
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 12,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 7.2. Gợi ý từ khóa (Autocomplete)

**Mô tả:** Gợi ý số hiệu và tên văn bản khi user gõ. Prefix match, ưu tiên số hiệu.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/documents/suggest` |
| **Auth** | Không |

**Query Params:**

| Param | Kiểu | Bắt buộc |
|-------|------|----------|
| `q` | string | ✅ (min 2 ký tự) |
| `limit` | int | ❌ (default 8) |

**Ví dụ:**

```
GET /api/documents/suggest?q=15/2022
```

**Response `200 OK`:**

```json
{
  "success": true,
  "data": [
    {
      "id": 42,
      "documentNumber": "15/2022/NĐ-CP",
      "title": "Nghị định quy định chi tiết Luật Xây dựng",
      "documentType": "NGHI_DINH"
    },
    {
      "id": 43,
      "documentNumber": "15/2022/TT-BXD",
      "title": "Thông tư hướng dẫn...",
      "documentType": "THONG_TU"
    }
  ],
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 7.3. Chi tiết văn bản

**Mô tả:** Metadata đầy đủ, trích yếu, link PDF. Tăng `viewCount`, ghi audit `VIEW` và `view_history` (nếu login). Optional query `highlight` để highlight từ khóa trong `contentText`.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/documents/{id}` |
| **Auth** | Không |

**Path Params:** `id` — ID văn bản

**Query Params:**

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `highlight` | string | Từ khóa cần `<mark>` trong nội dung |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "id": 42,
    "documentNumber": "15/2022/NĐ-CP",
    "title": "Nghị định quy định chi tiết Luật Xây dựng sửa đổi, bổ sung một số điều của Luật Xây dựng",
    "abstract": "Quy định chi tiết một số điều của Luật Xây dựng về quản lý hoạt động xây dựng...",
    "documentType": "NGHI_DINH",
    "issuingBody": "Chính phủ",
    "signer": "Phạm Minh Chính",
    "issuedDate": "2022-03-31",
    "effectiveDate": "2022-05-15",
    "expiryDate": null,
    "status": "CON_HIEU_LUC",
    "field": "XAY_DUNG",
    "pdfUrl": "https://res.cloudinary.com/.../15-2022-nd-cp.pdf",
    "pdfFileName": "15-2022-ND-CP.pdf",
    "pdfSizeBytes": 2048576,
    "sourceUrl": "https://...",
    "contentText": "Chính phủ...\n\nĐiều 1. Phạm vi điều chỉnh\n...",
    "contentHtml": "<p>Chính phủ...</p><h3>Điều 1. Phạm vi điều chỉnh</h3>...",
    "viewCount": 891,
    "downloadCount": 234,
    "categories": [
      { "id": 1, "name": "Giấy phép xây dựng", "slug": "giay-phep-xay-dung" }
    ],
    "tags": [
      { "id": 3, "name": "GPXD", "slug": "gpxd" }
    ],
    "bookmarked": false,
    "createdAt": "2026-06-01T08:00:00+07:00",
    "updatedAt": "2026-06-15T10:00:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

> `bookmarked`: chỉ có ý nghĩa khi request kèm Bearer token; guest luôn `false`.

**Lỗi:**

```json
// 404 NOT_FOUND
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Không tìm thấy văn bản với id=999"
  }
}
```

---

### 7.4. Tải PDF

**Mô tả:** Redirect hoặc stream file PDF. Ghi audit `DOWNLOAD`, tăng `downloadCount`.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/documents/{id}/download` |
| **Auth** | Không |

**Response `200 OK`:**

- **Option A — Redirect:** `302 Location: {pdfUrl}`
- **Option B — Stream:**

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="15-2022-ND-CP.pdf"
```

**Response JSON (nếu dùng signed URL):**

```json
{
  "success": true,
  "data": {
    "downloadUrl": "https://res.cloudinary.com/.../15-2022-nd-cp.pdf",
    "fileName": "15-2022-ND-CP.pdf",
    "expiresAt": "2026-07-03T15:30:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 7.7. Bộ lọc — Metadata cho UI

**Mô tả:** Trả danh sách giá trị filter (loại VB, cơ quan, năm) — cache được.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/documents/filters` |
| **Auth** | Không |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "documentTypes": [
      { "value": "LUAT", "label": "Luật", "count": 5 },
      { "value": "NGHI_DINH", "label": "Nghị định", "count": 42 }
    ],
    "statuses": [
      { "value": "CON_HIEU_LUC", "label": "Đang hiệu lực", "count": 98 },
      { "value": "HET_HIEU_LUC", "label": "Hết hiệu lực", "count": 30 }
    ],
    "issuingBodies": [
      { "value": "Chính phủ", "count": 55 },
      { "value": "Bộ Xây dựng", "count": 38 }
    ],
    "years": [2024, 2023, 2022, 2021, 2020, 2019, 2014]
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

## 8. Bookmarks API

> Yêu cầu **Bearer token** — role `USER` hoặc `ADMIN`.

### 8.1. Danh sách bookmark

**Mô tả:** Văn bản yêu thích của user hiện tại.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/bookmarks` |
| **Auth** | Bearer ✅ |

**Query Params:** `page`, `size`, `sort=createdAt,desc`

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 10,
        "document": {
          "id": 42,
          "documentNumber": "15/2022/NĐ-CP",
          "title": "Nghị định quy định chi tiết Luật Xây dựng",
          "documentType": "NGHI_DINH",
          "status": "CON_HIEU_LUC",
          "issuedDate": "2022-03-31"
        },
        "createdAt": "2026-07-01T09:00:00+07:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 8.2. Thêm bookmark

**Mô tả:** Lưu văn bản yêu thích.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/bookmarks/{documentId}` |
| **Auth** | Bearer ✅ |

**Path Params:** `documentId`

**Request Body:** Không

**Response `201 Created`:**

```json
{
  "success": true,
  "data": {
    "id": 10,
    "documentId": 42,
    "createdAt": "2026-07-03T14:30:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

**Lỗi:**

```json
// 409 — đã bookmark
{
  "success": false,
  "error": {
    "code": "CONFLICT",
    "message": "Văn bản đã có trong danh sách yêu thích"
  }
}
```

---

### 8.3. Xóa bookmark

**Mô tả:** Bỏ văn bản khỏi yêu thích.

| | |
|---|---|
| **Method** | `DELETE` |
| **Path** | `/api/bookmarks/{documentId}` |
| **Auth** | Bearer ✅ |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "message": "Đã xóa khỏi yêu thích"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 8.4. Kiểm tra trạng thái bookmark

**Mô tả:** Frontend dùng để hiển thị icon tim đã/chưa bookmark.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/bookmarks/{documentId}/status` |
| **Auth** | Bearer ✅ |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "documentId": 42,
    "bookmarked": true
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

## 9. Lịch sử người dùng API

> Yêu cầu **Bearer token**.

### 9.1. Lịch sử tìm kiếm

**Mô tả:** 20 tìm kiếm gần nhất của user.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/history/search` |
| **Auth** | Bearer ✅ |

**Query Params:** `page`, `size` (default size 20)

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 100,
        "query": "giấy phép xây dựng",
        "filters": {
          "type": "NGHI_DINH",
          "status": "CON_HIEU_LUC"
        },
        "resultCount": 12,
        "createdAt": "2026-07-03T10:00:00+07:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 9.2. Xóa toàn bộ lịch sử tìm kiếm

| | |
|---|---|
| **Method** | `DELETE` |
| **Path** | `/api/history/search` |
| **Auth** | Bearer ✅ |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": { "message": "Đã xóa lịch sử tìm kiếm" }
}
```

---

### 9.3. Lịch sử xem văn bản

**Mô tả:** Văn bản user đã xem gần đây.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/history/views` |
| **Auth** | Bearer ✅ |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 200,
        "document": {
          "id": 42,
          "documentNumber": "15/2022/NĐ-CP",
          "title": "Nghị định quy định chi tiết Luật Xây dựng",
          "documentType": "NGHI_DINH"
        },
        "viewedAt": "2026-07-03T13:00:00+07:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 8,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 9.4. Xóa lịch sử xem

| | |
|---|---|
| **Method** | `DELETE` |
| **Path** | `/api/history/views` |
| **Auth** | Bearer ✅ |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": { "message": "Đã xóa lịch sử xem văn bản" }
}
```

---

## 10. Hồ sơ cá nhân API

> Yêu cầu **Bearer token**. Pattern tham khảo User Management — **không** tách permission.

### 10.1. Lấy profile hiện tại

**GET** `/api/users/profile`

Lấy thông tin user từ access token.

#### Request Headers

```
Authorization: Bearer <access_token>
```

#### Response `200 OK`

```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "role": "USER",
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00+07:00",
    "updatedAt": "2026-07-03T14:30:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 10.2. Cập nhật profile

**PUT** `/api/users/profile`

Cập nhật họ tên (email không đổi qua API này).

#### Request Headers

```
Authorization: Bearer <access_token>
```

#### Request Body

```json
{
  "fullName": "Nguyễn Văn B"
}
```

#### Response `200 OK`

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn B",
    "role": "USER",
    "enabled": true,
    "updatedAt": "2026-07-03T15:00:00+07:00"
  },
  "timestamp": "2026-07-03T15:00:00+07:00"
}
```

---

### 10.3. Đổi mật khẩu

**PUT** `/api/users/change-password`

Đổi mật khẩu và **revoke tất cả refresh token** trong Redis (user phải đăng nhập lại mọi thiết bị).

#### Request Headers

```
Authorization: Bearer <access_token>
```

#### Request Body

```json
{
  "currentPassword": "Demo@123",
  "newPassword": "NewPass@456"
}
```

| Field | Kiểu | Bắt buộc |
|-------|------|----------|
| `currentPassword` | string | ✅ |
| `newPassword` | string | ✅ (min 8, có chữ hoa + số + ký tự đặc biệt) |

#### Response `200 OK`

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null,
  "timestamp": "2026-07-03T15:00:00+07:00"
}
```

#### Error Response `400 Bad Request`

```json
{
  "success": false,
  "message": "Mật khẩu hiện tại không đúng",
  "errors": [
    { "field": "currentPassword", "message": "Current password is incorrect" }
  ],
  "timestamp": "2026-07-03T15:00:00+07:00"
}
```

---

## 11. AI API

> Yêu cầu **Bearer token**. Rate limit Redis: **20 request/user/ngày** (có thể cấu hình).

**Disclaimer:** Response luôn kèm `disclaimer` — thông tin tham khảo, không thay thế văn bản gốc.

### 11.1. Tóm tắt văn bản

**Mô tả:** AI sinh 5 ý chính, đối tượng áp dụng, nội dung chính từ `content_text` / sections.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/ai/documents/{id}/summarize` |
| **Auth** | Bearer ✅ |

**Request Body:** Không (hoặc `{}`)

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "documentId": 42,
    "documentNumber": "15/2022/NĐ-CP",
    "summary": {
      "mainPoints": [
        "Quy định chi tiết điều kiện cấp giấy phép xây dựng",
        "Bổ sung quy trình thẩm định thiết kế",
        "Quy định trình tự nghiệm thu công trình",
        "Xử lý vi phạm trong hoạt động xây dựng",
        "Chuyển tiếp các quy định từ văn bản cũ"
      ],
      "applicableSubjects": "Chủ đầu tư, tổ chức, cá nhân hoạt động xây dựng; cơ quan quản lý nhà nước về xây dựng",
      "mainContent": "Nghị định quy định chi tiết Luật Xây dựng về quản lý hoạt động xây dựng, bao gồm cấp phép, thiết kế, thi công và nghiệm thu..."
    },
    "sources": [
      {
        "documentNumber": "15/2022/NĐ-CP",
        "sectionLabel": "Toàn văn",
        "excerpt": "Nghị định này quy định chi tiết một số điều của Luật Xây dựng..."
      }
    ],
    "model": "gemini-1.5-flash",
    "disclaimer": "Thông tin do AI tổng hợp, chỉ mang tính tham khảo. Vui lòng đối chiếu văn bản gốc."
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

**Lỗi rate limit:**

```json
// 429 RATE_LIMIT_EXCEEDED
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Bạn đã vượt quá 20 lượt sử dụng AI hôm nay",
    "details": [{ "field": "limit", "message": "20" }, { "field": "resetAt", "message": "2026-07-04T00:00:00+07:00" }]
  }
}
```

---

### 11.2. Hỏi đáp theo văn bản

**Mô tả:** User đặt câu hỏi; backend gửi nội dung văn bản + câu hỏi tới Gemini. Trả lời kèm trích dẫn Điều/Khoản.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/ai/documents/{id}/ask` |
| **Auth** | Bearer ✅ |

**Request Body:**

```json
{
  "question": "Điều kiện cấp giấy phép xây dựng là gì?"
}
```

| Field | Kiểu | Bắt buộc | Validation |
|-------|------|----------|------------|
| `question` | string | ✅ | 5–500 ký tự |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "documentId": 42,
    "question": "Điều kiện cấp giấy phép xây dựng là gì?",
    "answer": "Theo Nghị định 15/2022/NĐ-CP, điều kiện cấp giấy phép xây dựng bao gồm: (1) Có quyền sử dụng đất hợp pháp; (2) Có hồ sơ thiết kế được phê duyệt; (3) Đảm bảo các yêu cầu về phòng cháy chữa cháy...",
    "sources": [
      {
        "documentNumber": "15/2022/NĐ-CP",
        "sectionLabel": "Điều 89",
        "excerpt": "Giấy phép xây dựng được cấp khi đáp ứng các điều kiện sau: a) Có quyền sử dụng đất..."
      },
      {
        "documentNumber": "15/2022/NĐ-CP",
        "sectionLabel": "Khoản 2 Điều 15",
        "excerpt": "Hồ sơ cấp giấy phép xây dựng bao gồm..."
      }
    ],
    "model": "gemini-1.5-flash",
    "disclaimer": "Thông tin do AI tổng hợp, chỉ mang tính tham khảo. Vui lòng đối chiếu văn bản gốc."
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 11.3. Giải thích điều luật (đoạn bôi đen)

**Mô tả:** User chọn/bôi đen đoạn text (Điều, Khoản); AI giải thích bằng ngôn ngữ dễ hiểu.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/ai/documents/{id}/explain` |
| **Auth** | Bearer ✅ |

**Request Body:**

```json
{
  "selectedText": "Khoản 2 Điều 56",
  "context": "Tổ chức, cá nhân có hành vi vi phạm quy định về xây dựng bị xử phạt theo quy định của pháp luật về xử lý vi phạm hành chính."
}
```

| Field | Kiểu | Bắt buộc |
|-------|------|----------|
| `selectedText` | string | ✅ |
| `context` | string | ❌ (đoạn văn bản xung quanh — giúp AI chính xác hơn) |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "documentId": 42,
    "selectedText": "Khoản 2 Điều 56",
    "explanation": "Hiểu đơn giản là: nếu tổ chức hoặc cá nhân vi phạm các quy định về xây dựng (ví dụ xây không phép, sai thiết kế...), sẽ bị phạt tiền hoặc các hình thức xử lý khác theo luật xử phạt vi phạm hành chính — tức là bị cơ quan nhà nước có thẩm quyền 'phạt' chứ không phải xử lý hình sự.",
    "sources": [
      {
        "documentNumber": "15/2022/NĐ-CP",
        "sectionLabel": "Khoản 2 Điều 56",
        "excerpt": "Tổ chức, cá nhân có hành vi vi phạm quy định về xây dựng bị xử phạt..."
      }
    ],
    "model": "gemini-1.5-flash",
    "disclaimer": "Thông tin do AI tổng hợp, chỉ mang tính tham khảo. Vui lòng đối chiếu văn bản gốc."
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 11.4. Kiểm tra quota AI còn lại

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/ai/quota` |
| **Auth** | Bearer ✅ |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "dailyLimit": 20,
    "usedToday": 3,
    "remaining": 17,
    "resetAt": "2026-07-04T00:00:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

## 12. Admin — Dashboard

> Prefix `/api/admin/**` — yêu cầu role **ADMIN**.

### 12.1. Thống kê tổng quan

**Mô tả:** Số liệu dashboard admin.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/admin/dashboard` |
| **Auth** | Bearer ✅ ADMIN |

**Query Params:**

| Param | Kiểu | Mặc định |
|-------|------|----------|
| `period` | string | `7d` — `7d` \| `30d` \| `all` |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "overview": {
      "totalDocuments": 128,
      "totalUsers": 45,
      "totalSearches": 1250,
      "totalViews": 8900,
      "totalDownloads": 2100,
      "totalAiRequests": 320
    },
    "topKeywords": [
      { "keyword": "giấy phép xây dựng", "count": 156 },
      { "keyword": "nghiệm thu", "count": 98 },
      { "keyword": "PCCC", "count": 87 }
    ],
    "topDocuments": [
      {
        "id": 1,
        "documentNumber": "50/2014/QH13",
        "title": "Luật Xây dựng",
        "viewCount": 1520
      }
    ],
    "recentActivity": [
      {
        "eventType": "SEARCH",
        "metadata": { "query": "quy hoạch" },
        "createdAt": "2026-07-03T14:25:00+07:00"
      }
    ],
    "period": "7d"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

## 13. Admin — Quản lý văn bản

### 13.1. Danh sách văn bản (admin)

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/admin/documents` |
| **Auth** | ADMIN |

**Query Params:** Giống search + `includeDeleted=true`

**Response `200 OK`:** Page `DocumentAdminDto` (thêm `deletedAt`, `sourceUrl`)

---

### 13.2. Tạo văn bản

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/admin/documents` |
| **Auth** | ADMIN |

**Request Body:**

```json
{
  "documentNumber": "99/2026/NĐ-CP",
  "title": "Nghị định mẫu",
  "abstract": "Trích yếu...",
  "documentType": "NGHI_DINH",
  "issuingBody": "Chính phủ",
  "signer": "Phạm Minh Chính",
  "issuedDate": "2026-01-15",
  "effectiveDate": "2026-03-01",
  "expiryDate": null,
  "status": "CON_HIEU_LUC",
  "contentText": "Toàn văn văn bản...",
  "sourceUrl": "https://...",
  "categoryIds": [1, 2],
  "tagIds": [3]
}
```

**Response `201 Created`:**

```json
{
  "success": true,
  "data": {
    "id": 129,
    "documentNumber": "99/2026/NĐ-CP",
    "documentNumberNormalized": "99/2026/ND-CP",
    "title": "Nghị định mẫu",
    "createdAt": "2026-07-03T14:30:00+07:00"
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

### 13.3. Cập nhật văn bản

| | |
|---|---|
| **Method** | `PUT` |
| **Path** | `/api/admin/documents/{id}` |
| **Auth** | ADMIN |

**Request Body:** Cùng cấu trúc [13.2](#132-tạo-văn-bản)

**Response `200 OK`:** Document đã cập nhật

---

### 13.4. Xóa văn bản (soft delete)

| | |
|---|---|
| **Method** | `DELETE` |
| **Path** | `/api/admin/documents/{id}` |
| **Auth** | ADMIN |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": { "message": "Đã xóa văn bản", "deletedAt": "2026-07-03T14:30:00+07:00" }
}
```

---

### 13.5. Khôi phục văn bản

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/admin/documents/{id}/restore` |
| **Auth** | ADMIN |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": { "message": "Đã khôi phục văn bản" }
}
```

---

### 13.6. Upload PDF

**Mô tả:** Upload file PDF, lưu storage, auto extract `contentText` + `searchText`.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/admin/documents/{id}/upload-pdf` |
| **Auth** | ADMIN |
| **Content-Type** | `multipart/form-data` |

**Request (form-data):**

| Field | Kiểu | Bắt buộc |
|-------|------|----------|
| `file` | file | ✅ (.pdf, max 20MB) |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "documentId": 42,
    "pdfUrl": "https://res.cloudinary.com/.../15-2022-nd-cp.pdf",
    "pdfFileName": "15-2022-ND-CP.pdf",
    "pdfSizeBytes": 2048576,
    "contentTextExtracted": true,
    "contentTextLength": 125000
  },
  "timestamp": "2026-07-03T14:30:00+07:00"
}
```

---

## 14. Admin — Danh mục & Tag

### 14.1. CRUD Categories

| Method | Path | Mô tả |
|--------|------|-------|
| `GET` | `/api/admin/categories` | Danh sách |
| `POST` | `/api/admin/categories` | Tạo |
| `PUT` | `/api/admin/categories/{id}` | Sửa |
| `DELETE` | `/api/admin/categories/{id}` | Xóa |

**POST Request:**

```json
{
  "name": "Giấy phép xây dựng",
  "slug": "giay-phep-xay-dung",
  "description": "Các văn bản về GPXD",
  "displayOrder": 1
}
```

**Response `201`:**

```json
{
  "success": true,
  "data": {
    "id": 7,
    "name": "Giấy phép xây dựng",
    "slug": "giay-phep-xay-dung",
    "displayOrder": 1
  }
}
```

---

### 14.2. CRUD Tags

| Method | Path | Mô tả |
|--------|------|-------|
| `GET` | `/api/admin/tags` | Danh sách |
| `POST` | `/api/admin/tags` | Tạo |
| `PUT` | `/api/admin/tags/{id}` | Sửa |
| `DELETE` | `/api/admin/tags/{id}` | Xóa |

**POST Request:**

```json
{
  "name": "GPXD",
  "slug": "gpxd"
}
```

---

## 15. Admin — Quản lý User

### 15.1. Danh sách user

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/admin/users` |
| **Auth** | ADMIN |

**Query Params:** `page`, `size`, `q` (search email/name), `enabled`, `role`

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user@demo.com",
        "fullName": "Demo User",
        "role": "USER",
        "enabled": true,
        "createdAt": "2026-06-01T10:00:00+07:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

---

### 15.2. Khóa user

**Mô tả:** `enabled = false` + revoke all refresh token Redis.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/admin/users/{id}/disable` |
| **Auth** | ADMIN |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": { "message": "Đã khóa tài khoản", "enabled": false }
}
```

---

### 15.3. Mở khóa user

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/admin/users/{id}/enable` |
| **Auth** | ADMIN |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": { "message": "Đã mở khóa tài khoản", "enabled": true }
}
```

---

### 15.4. Đổi role user

| | |
|---|---|
| **Method** | `PATCH` |
| **Path** | `/api/admin/users/{id}/role` |
| **Auth** | ADMIN |

**Request Body:**

```json
{
  "role": "ADMIN"
}
```

---

## 16. Admin — Crawl & Nhật ký đồng bộ

> **Optional** — phục vụ refresh dataset. Production demo có thể ẩn menu.

### 16.1. Trigger đồng bộ dữ liệu

**Mô tả:** Kích hoạt job crawl (async). Backend gọi Python script hoặc internal service. Ghi `crawl_logs`.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/admin/crawl/sync` |
| **Auth** | ADMIN |

**Request Body (optional):**

```json
{
  "mode": "incremental",
  "source": "all"
}
```

| Field | Giá trị |
|-------|---------|
| `mode` | `full` \| `incremental` |
| `source` | `all` \| tên nguồn cụ thể |

**Response `202 Accepted`:**

```json
{
  "success": true,
  "data": {
    "crawlLogId": 15,
    "status": "RUNNING",
    "message": "Đồng bộ dữ liệu đang chạy. Vui lòng theo dõi nhật ký.",
    "startedAt": "2026-07-03T14:30:00+07:00"
  }
}
```

---

### 16.2. Danh sách nhật ký đồng bộ

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/admin/crawl/logs` |
| **Auth** | ADMIN |

**Query Params:** `page`, `size`, `status`

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 15,
        "status": "SUCCESS",
        "triggeredBy": "MANUAL",
        "triggeredByUserId": 2,
        "startedAt": "2026-07-03T14:30:00+07:00",
        "finishedAt": "2026-07-03T14:45:00+07:00",
        "insertedCount": 5,
        "updatedCount": 12,
        "skippedCount": 111,
        "errorCount": 0,
        "errorDetails": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

### 16.3. Chi tiết nhật ký đồng bộ

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/admin/crawl/logs/{id}` |
| **Auth** | ADMIN |

**Response `200 OK`:**

```json
{
  "success": true,
  "data": {
    "id": 15,
    "status": "PARTIAL",
    "triggeredBy": "CLI",
    "startedAt": "2026-07-03T14:30:00+07:00",
    "finishedAt": "2026-07-03T14:45:00+07:00",
    "insertedCount": 5,
    "updatedCount": 12,
    "skippedCount": 110,
    "errorCount": 1,
    "errorDetails": [
      {
        "documentNumber": "XX/2026/TT-BXD",
        "message": "Không tải được PDF",
        "sourceUrl": "https://..."
      }
    ],
    "notes": "Crawl one-shot từ local"
  }
}
```

---

## 17. Health & Metadata

### 17.1. Health check

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/actuator/health` |
| **Auth** | Không |

**Response `200 OK`:**

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

### 17.2. Swagger UI

| | |
|---|---|
| **URL** | `/swagger-ui.html` |
| **OpenAPI JSON** | `/v3/api-docs` |

> Production: disable hoặc bảo vệ bằng auth / profile `dev` only.

---

## 18. Enum tham chiếu

Đồng bộ với [DATABASE.md](./DATABASE.md#5-enum--hằng-số-nghiệp-vụ).

### `documentType`

`LUAT` · `NGHI_DINH` · `THONG_TU` · `QUYET_DINH` · `QCVN` · `TCVN` · `CONG_VAN` · `KHAC`

### `status` (hiệu lực)

`CON_HIEU_LUC` · `HET_HIEU_LUC` · `CHUA_CO_HIEU_LUC` · `HET_HIEU_LUC_MOT_PHAN`

### `relationType`

`GUIDED_BY` · `GUIDES` · `AMENDED_BY` · `AMENDS` · `REPLACES` · `REPLACED_BY` · `RELATED`

### `role`

`USER` · `ADMIN`

### `auditEventType`

`SEARCH` · `VIEW` · `DOWNLOAD` · `AI_SUMMARIZE` · `AI_ASK` · `AI_EXPLAIN`

### `crawlStatus`

`RUNNING` · `SUCCESS` · `PARTIAL` · `FAILED`

---

## 19. Ma trận phân quyền

> Chỉ dùng **role** (`USER` / `ADMIN`) — **không** có permission per-endpoint.

| Endpoint group | Guest | USER | ADMIN |
|----------------|:-----:|:----:|:-----:|
| `/api/public/**` | ✅ | ✅ | ✅ |
| `/api/documents/**` (read) | ✅ | ✅ | ✅ |
| `/api/auth/register`, `login`, `refresh` | ✅ | ✅ | ✅ |
| `/api/auth/logout` | ❌ | ✅ | ✅ |
| `/api/users/profile`, `change-password` | ❌ | ✅ | ✅ |
| `/api/bookmarks/**` | ❌ | ✅ | ✅ |
| `/api/history/**` | ❌ | ✅ | ✅ |
| `/api/ai/**` | ❌ | ✅ | ✅ |
| `/api/admin/**` | ❌ | ❌ | ✅ (role ADMIN) |

---

## Phụ lục A — Tổng hợp endpoint

| # | Method | Path | Auth |
|---|--------|------|------|
| 1 | POST | `/api/auth/register` | — |
| 2 | POST | `/api/auth/login` | — |
| 3 | POST | `/api/auth/refresh` | Cookie |
| 4 | POST | `/api/auth/logout` | Bearer + Cookie |
| 5 | GET | `/api/users/profile` | Bearer |
| 6 | PUT | `/api/users/profile` | Bearer |
| 7 | PUT | `/api/users/change-password` | Bearer |
| 8 | GET | `/api/public/stats` | — |
| 9 | GET | `/api/public/documents/recent` | — |
| 10 | GET | `/api/public/documents/popular` | — |
| 11 | GET | `/api/public/documents/updated` | — |
| 12 | GET | `/api/public/categories` | — |
| 13 | GET | `/api/documents/search` | — |
| 14 | GET | `/api/documents/suggest` | — |
| 15 | GET | `/api/documents/filters` | — |
| 16 | GET | `/api/documents/{id}` | — |
| 17 | GET | `/api/documents/{id}/sections` | — |
| 18 | GET | `/api/documents/{id}/relations` | — |
| 19 | GET | `/api/documents/{id}/download` | — |
| 20 | GET | `/api/bookmarks` | Bearer |
| 21 | POST | `/api/bookmarks/{documentId}` | Bearer |
| 22 | DELETE | `/api/bookmarks/{documentId}` | Bearer |
| 23 | GET | `/api/bookmarks/{documentId}/status` | Bearer |
| 24 | GET | `/api/history/search` | Bearer |
| 25 | DELETE | `/api/history/search` | Bearer |
| 26 | GET | `/api/history/views` | Bearer |
| 27 | DELETE | `/api/history/views` | Bearer |
| 28 | POST | `/api/ai/documents/{id}/summarize` | Bearer |
| 29 | POST | `/api/ai/documents/{id}/ask` | Bearer |
| 30 | POST | `/api/ai/documents/{id}/explain` | Bearer |
| 31 | GET | `/api/ai/quota` | Bearer |
| 32 | GET | `/api/admin/dashboard` | ADMIN |
| 33 | GET | `/api/admin/documents` | ADMIN |
| 34 | POST | `/api/admin/documents` | ADMIN |
| 35 | PUT | `/api/admin/documents/{id}` | ADMIN |
| 36 | DELETE | `/api/admin/documents/{id}` | ADMIN |
| 37 | POST | `/api/admin/documents/{id}/restore` | ADMIN |
| 38 | POST | `/api/admin/documents/{id}/upload-pdf` | ADMIN |
| 39 | GET | `/api/admin/documents/{id}/relations` | ADMIN |
| 40 | POST | `/api/admin/relations` | ADMIN |
| 41 | DELETE | `/api/admin/relations/{id}` | ADMIN |
| 42–45 | CRUD | `/api/admin/categories/**` | ADMIN |
| 46–49 | CRUD | `/api/admin/tags/**` | ADMIN |
| 50 | GET | `/api/admin/users` | ADMIN |
| 51 | POST | `/api/admin/users/{id}/disable` | ADMIN |
| 52 | POST | `/api/admin/users/{id}/enable` | ADMIN |
| 53 | PATCH | `/api/admin/users/{id}/role` | ADMIN |
| 54 | POST | `/api/admin/crawl/sync` | ADMIN |
| 55 | GET | `/api/admin/crawl/logs` | ADMIN |
| 56 | GET | `/api/admin/crawl/logs/{id}` | ADMIN |

**Tổng: 56 endpoints** (4 auth + 3 profile + 49 còn lại)

---

## Phụ lục B — Axios interceptor mẫu

```javascript
// axios instance: baseURL = VITE_API_URL, withCredentials = true (gửi cookie refresh)
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      try {
        const { data } = await api.post('/auth/refresh'); // cookie tự gửi
        const { accessToken } = data.data;
        localStorage.setItem('accessToken', accessToken);
        original.headers.Authorization = `Bearer ${accessToken}`;
        return api(original);
      } catch {
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Logout — gửi accessToken trong body để server blacklist jti
export async function logout() {
  const accessToken = localStorage.getItem('accessToken');
  await api.post('/auth/logout', { accessToken });
  localStorage.removeItem('accessToken');
}
```

---

## Phụ lục C — Liên kết tài liệu

| Tài liệu | Mô tả |
|----------|--------|
| [DATABASE.md](./DATABASE.md) | Schema, ERD, enum, index |
| Swagger UI | `/swagger-ui.html` (khi backend chạy) |

---

*Tài liệu này là contract API baseline. Cập nhật version khi thêm/sửa endpoint.*

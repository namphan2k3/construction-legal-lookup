# Construction Legal Lookup

Hệ thống tra cứu văn bản pháp luật ngành xây dựng tích hợp AI.

## Tính năng chính

### Công khai
- Trang chủ với thống kê tổng quan (số văn bản, loại, cơ quan ban hành)
- Danh sách văn bản mới ban hành
- Danh sách văn bản nổi bật (xem nhiều)
- Danh sách văn bản mới cập nhật
- Danh sách danh mục
- Tra cứu văn bản (full-text search, bộ lọc theo loại, cơ quan ban hành, năm, trạng thái hiệu lực, danh mục, tag)
- Gợi ý từ khóa autocomplete
- Xem chi tiết văn bản (metadata, nội dung, danh mục, tag, xem nhiều, tải xuống)
- Tải xuống PDF
- Bộ lọc metadata (loại văn bản, trạng thái, cơ quan ban hành, năm)

### Người dùng đã đăng nhập
- Quản lý bookmark (thêm, xóa, xem danh sách, kiểm tra trạng thái)
- Lịch sử tìm kiếm và xem văn bản
- Hồ sơ cá nhân (xem, cập nhật thông tin, đổi mật khẩu)
- Tính năng AI:
  - Tóm tắt văn bản
  - Hỏi đáp về văn bản
  - Chatbot trợ lý pháp luật chuyên ngành xây dựng
  - Kiểm tra hạn ngạch AI (mặc định 20 lượt/ngày)

### Admin
- Dashboard tổng quan (thống kê số liệu, từ khóa tìm kiếm phổ biến, văn bản xem nhiều, hoạt động gần đây)
- Quản lý văn bản (tạo, sửa, xóa mềm, khôi phục, upload PDF)
- Quản lý danh mục (CRUD)
- Quản lý tag (CRUD)
- Quản lý người dùng (xem danh sách, sửa thông tin, khóa/mở khóa, thay đổi vai trò)

## Công nghệ sử dụng

- Backend: Spring Boot 3.x, Spring Security, Spring Data JPA
- Frontend: React, Vite, Tailwind CSS, lucide-react
- Database: MySQL 8.x (Flyway migrations)
- Cache & Rate limiting: Redis 7.x
- Tích hợp AI: Google Gemini
- Cloud storage: Cloudinary (PDF)
- Containerization: Docker, Docker Compose

## Bắt đầu

### Yêu cầu

- Java 17+
- Maven 3.9+
- Node.js 18+ & npm
- Docker & Docker Compose (để chạy MySQL & Redis)

### Cài đặt & chạy

1. Clone repo:
   ```bash
   git clone <repo-url>
   cd ConstructionLegalLookup
   ```

2. Khởi động MySQL & Redis:
   ```bash
   docker-compose up -d
   ```

3. Cấu hình môi trường:
   - Backend: Copy và cập nhật `construction-legal-lookup-app/src/main/resources/application-dev.yaml` với các giá trị cần thiết (API keys, database info,...)
   - Frontend: Copy và cập nhật `frontend/.env` (nếu cần)

4. Chạy backend:
   ```bash
   cd construction-legal-lookup-app
   mvn spring-boot:run
   ```

5. Chạy frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

6. Truy cập:
   - Frontend: `http://localhost:5173`
   - API: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

## Tài khoản demo

- Admin: admin@example.com / password123
- User: user@example.com / password123

## Tài liệu tham khảo

- API Documentation: docs/API.md
- Database Design: docs/DATABASE.md

## Đóng góp

Mọi đóng góp đều được chào đón!

## License

MIT
